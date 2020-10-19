/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017-2018 ForgeRock AS.
 */


package com.example.customAuthNode;

import com.google.common.base.Strings;
import static org.forgerock.openam.auth.node.api.Action.send;

import java.util.regex.Pattern;

import javax.inject.Inject;
 
import org.forgerock.openam.auth.node.api.SingleOutcomeNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.auth.nodes.webauthn.ClientScriptUtilities;
import org.forgerock.openam.core.realms.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.forgerock.json.JsonValue;

import com.google.inject.assistedinject.Assisted;

import com.google.common.collect.ImmutableList;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.shared.validation.URLValidator;
import com.sun.identity.sm.RequiredValueValidator;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.security.auth.callback.Callback;
import org.forgerock.openam.annotations.sm.Attribute;



/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider  = SingleOutcomeNode.OutcomeProvider.class,
               configClass      = VoisentryAudioCollector.Config.class)
public class VoisentryAudioCollector extends SingleOutcomeNode {

    private final Pattern DN_PATTERN = Pattern.compile("^[a-zA-Z0-9]=([^,]+),");
    private final Logger logger = LoggerFactory.getLogger(VoisentryAudioCollector.class);
    private final Config config;
    private final Realm realm;

    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * Clear the audio sources kept in the shared state
         */
        @Attribute(order = 10)
        default boolean clearAudioSource() {
            return false;
        }
        
        /**
         * The recording time, 0 = infinite => recording stop by the button
         */
        @Attribute(order = 20, validators = RequiredValueValidator.class)
        default long recordingTime() {
            return 0;
        }
        
        /**
         * The contents of the page header div
         */
        @Attribute(order = 30)
        default String headContents() {
            return "";
        }
        
        /**
         * The contents of the page footer div
         */
        @Attribute(order = 40)
        default String footContents() {
            return "";
        }

        /**
         * The contents of the page mic div
         */
        @Attribute(order = 50)
        default String micButtonContents() {
            return "";
        }
        
        /**
         * The css file URL to style the page look.
         */
        @Attribute(order = 60, validators = URLValidator.class)
        default String cssFileUrl() {
            return "";
        }

        /**
         * Clear the audio sources kept in the shared state
         */
        @Attribute(order = 70, validators = URLValidator.class)
        default String jsFileUrl() {
            return "";
        }


    }


    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     * @param realm The realm the node is in.
     * @throws NodeProcessException If the configuration was not valid.
     */
    @Inject
    public VoisentryAudioCollector(@Assisted Config config, @Assisted Realm realm) throws NodeProcessException {
        this.config = config;
        this.realm = realm;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        
        logger.error("Voisentry Audio Collector node started...");
        
        String myScript = getScriptAsString("/home/ubuntu/tomcat/bin/js/voisentryAudioCollector.js",
                                            config.cssFileUrl(),
                                            config.jsFileUrl(),
                                            VoisentryConstants.AUDIOSOURCE,
                                            config.headContents(),
                                            config.footContents(),
                                            config.micButtonContents(),
                                            config.recordingTime());
        //logger.error ("Javascript myscript: " + myScript);

        Optional<String> result = context.getCallback(HiddenValueCallback.class).map(HiddenValueCallback::getValue).
                filter(scriptOutput -> !Strings.isNullOrEmpty(scriptOutput));
        if (result.isPresent()) {
            String resultValue = result.get();
            logger.error ("Audiosource result present: " + resultValue);
            if ("undefined".equalsIgnoreCase(resultValue)) {
                resultValue = "Not set";
                throw new NodeProcessException("Audio source is undefined, check js code");
            }
            
            logger.error("Get the shared state");
            JsonValue sharedState = context.sharedState;
            
            List<String> audioSource;
            audioSource = null;
            if (config.clearAudioSource() == false) {
                audioSource = sharedState.get(VoisentryConstants.AUDIOSOURCE).asList(String.class);
            }
            if (audioSource == null) {
                audioSource = new ArrayList<>();
            }
            
            logger.error("Audiosource number: " + audioSource.size());
            
            audioSource.add(resultValue);
            
            JsonValue newSharedState = context.sharedState.copy();
            newSharedState.put(VoisentryConstants.AUDIOSOURCE, audioSource);
            return goToNext().replaceSharedState(newSharedState).build();
            
        } else {
            
            logger.error ("Audiosource result not present, sending callbacks");
            String clientSideScriptExecutorFunction = createClientSideScriptExecutorFunction(myScript);

            ScriptTextOutputCallback scriptAndSelfSubmitCallback =
                    new ScriptTextOutputCallback(clientSideScriptExecutorFunction);

            HiddenValueCallback hiddenValueCallback = new HiddenValueCallback(VoisentryConstants.AUDIOSOURCE, "false");
            ImmutableList<Callback> callbacks = ImmutableList.of(scriptAndSelfSubmitCallback, hiddenValueCallback);
            
            return send(callbacks).build();
            
        }
        
    }
    
    
    private String getScriptAsString(String filename,
                                     String cssFileUrl,
                                     String jsFileUrl,
                                     String audioSourceId,
                                     String acuHeader,
                                     String acuFooter,
                                     String acuMicButton,
                                     long   recordingTime) {
        
        if (acuHeader == null) {
            acuHeader = "";
        }
        
        if (acuFooter == null) {
            acuFooter = "";
        }
        
        if (jsFileUrl == null) {
            jsFileUrl = "";
        }
        
        if (acuMicButton == null) {
            acuMicButton = "<div class='object'><div class='button'></div><div class='button' id='circlein'><svg class='mic-icon' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' x='0px' y='0px' viewBox='0 0 1000 1000' enable-background='new 0 0 1000 1000' xml:space='preserve' style='fill:#FFFFFF'><g><path d='M500,683.8c84.6,0,153.1-68.6,153.1-153.1V163.1C653.1,78.6,584.6,10,500,10c-84.6,0-153.1,68.6-153.1,153.1v367.5C346.9,615.2,415.4,683.8,500,683.8z M714.4,438.8v91.9C714.4,649,618.4,745,500,745c-118.4,0-214.4-96-214.4-214.4v-91.9h-61.3v91.9c0,141.9,107.2,258.7,245,273.9v124.2H346.9V990h306.3v-61.3H530.6V804.5c137.8-15.2,245-132.1,245-273.9v-91.9H714.4z'/></g></svg></div></div>";
        }

        try {
            //logger.error("Reading stream: " + filename);
            Reader paramReader = new InputStreamReader(new FileInputStream(filename));

            StringBuilder data = new StringBuilder();
            BufferedReader objReader = new BufferedReader(paramReader);
            String strCurrentLine;
            while ((strCurrentLine = objReader.readLine()) != null) {
                //logger.error("Current line" + strCurrentLine);
                data.append(strCurrentLine).append(System.lineSeparator());
            }
            //logger.error("Read data: " + data.toString());
            if (acuMicButton.isEmpty()) {
                acuMicButton = "<button id='acuMicButton' class='acuMicButton'>&#xf130;</button>";
            }
            return String.format(data.toString(), cssFileUrl, audioSourceId, acuHeader, acuFooter, acuMicButton, recordingTime, jsFileUrl);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }
    
    private static String createClientSideScriptExecutorFunction(String script) {
        return String.format(
                "(function(output) {\n" +
                        "    %s\n" + // script
                        "}) (document);\n",
                script
        );
    }
  
}
