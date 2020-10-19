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
import org.forgerock.openam.core.realms.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.forgerock.json.JsonValue;

import com.google.inject.assistedinject.Assisted;

import java.util.ResourceBundle;
import javax.security.auth.callback.NameCallback;



/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider  = SingleOutcomeNode.OutcomeProvider.class,
               configClass      = VoisentryEnrolidCollectorNode.Config.class)
public class VoisentryEnrolidCollectorNode extends SingleOutcomeNode {

    private final Pattern DN_PATTERN = Pattern.compile("^[a-zA-Z0-9]=([^,]+),");
    private final Logger logger = LoggerFactory.getLogger(VoisentryEnrolidCollectorNode.class);
    private final Config config;
    private final Realm realm;

    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * The header name for zero-page login that will contain the identity's username.
         */
    }
    
    private static final String BUNDLE = "com/example/customAuthNode/VoisentryEnrolidCollectorNode";


    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     * @param realm The realm the node is in.
     * @throws NodeProcessException If the configuration was not valid.
     */
    @Inject
    public VoisentryEnrolidCollectorNode(@Assisted Config config, @Assisted Realm realm) throws NodeProcessException {
        this.config = config;
        this.realm = realm;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        
        logger.error("Voisentry Enrolid Collector node started...");
        
        JsonValue sharedState = context.sharedState;
        return context.getCallback(NameCallback.class)
                .map(NameCallback::getName)
                .filter(password -> !Strings.isNullOrEmpty(password))
                .map(name -> goToNext().replaceSharedState(sharedState.copy().put(VoisentryConstants.ENROLID, name)).build())
                .orElseGet(() -> collectUsername(context));
    }

    private Action collectUsername(TreeContext context) {
        ResourceBundle bundle = context.request.locales.getBundleInPreferredLocale(BUNDLE, getClass().getClassLoader());
        logger.error("collecting enrolid");
        return send(new NameCallback(bundle.getString("callback.username"))).build();
    }
  
}
