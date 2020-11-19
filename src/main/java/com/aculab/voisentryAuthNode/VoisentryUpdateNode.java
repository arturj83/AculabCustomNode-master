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
// */

package com.aculab.voisentryAuthNode;

import java.util.regex.Pattern;

import javax.inject.Inject;

import org.forgerock.openam.auth.node.api.AbstractDecisionNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.realms.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;
import com.iplanet.sso.SSOException;
import com.sun.identity.sm.SMSException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;


import org.forgerock.json.JsonValue;
import org.forgerock.json.JsonValueException;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.sm.AnnotatedServiceRegistry;
import org.forgerock.util.i18n.PreferredLocales;


/**
 * A node that sends the Update request to Voisentry server
 * @author artur.jablonski@aculab.com
 */
@Node.Metadata(outcomeProvider  = VoisentryUpdateNode.VoisentryUpdateNodeOutcomeProvider.class,
               configClass      = VoisentryUpdateNode.Config.class)
public class VoisentryUpdateNode extends AbstractDecisionNode {

    private final Pattern DN_PATTERN = Pattern.compile("^[a-zA-Z0-9]=([^,]+),");
    private final Logger logger = LoggerFactory.getLogger(VoisentryUpdateNode.class);
    private final VoisentryService serviceConfig;
    private final Config config;
    private final Realm realm;

    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * The voisentry node URL used to receive the audio sample.
         */
        @Attribute(order = 10)
        default String voisentryNodeUrl() {
            return "";
        }

        /**
         * The voisentry dataset key used to store the voiceprints.
         */
        @Attribute(order = 20)
        default String voisentryDatasetKey() {
            return "";
        }
        
        /**
         * The enrolid source.
         */
        @Attribute(order = 30)
        default VoisentryConstants.GetEnrolId getEnrolId() { 
            return VoisentryConstants.GetEnrolId.SERVICE;
        }
        
        /**
        * The id repository field name for the enrolid source
        */
        @Attribute(order = 40)
        default String idRepoEnrolidName() {
            return "";
        }
        
        /**
         * The configurable error outcomes for this node
         */
        @Attribute(order = 50)
        default Set<VoisentryErrorCode> errorCodeOutcomes() {
            Set<VoisentryErrorCode> errorCodeOutcomes = new LinkedHashSet<>();
            return errorCodeOutcomes;
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
    public VoisentryUpdateNode(@Assisted Config config, @Assisted Realm realm, AnnotatedServiceRegistry serviceRegistry) throws NodeProcessException {
        this.config = config;
        this.realm = realm;
        try {
            this.serviceConfig = serviceRegistry.getRealmSingleton(VoisentryService.class, realm).get();
        } catch (SSOException | SMSException e) {
            throw new NodeProcessException(e);
        }
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        
        logger.error("VoisentryUpdateNode started");
        
        logger.error("Get voisentry node URL");
        String voisentryNodeUrl = VoisentryConstants.getVoisentryNodeUrl(config.voisentryNodeUrl(),
                                                                         serviceConfig.voisentryNodeUrl(),
                                                                         logger);

        logger.error("Get voisentry dataset key");
        String voisentryDatasetKey = VoisentryConstants.getVoisentryDatasetKey(config.voisentryDatasetKey(),
                                                                               serviceConfig.voisentryDatasetKey(),
                                                                               logger);
        
        logger.error("Get the shared state");
        JsonValue sharedState = context.sharedState;
        
        //get the enrolId from the config
        String enrolId = VoisentryConstants.getEnrolIdFromConfig(sharedState, realm, config.getEnrolId(), config.idRepoEnrolidName(), serviceConfig, logger);

        if (enrolId == null || enrolId.isEmpty()) {
            logger.error("Failed to get the enrolid from the config");
            throw new NodeProcessException("Failed to get the enrolid from the config");
        }
        
        if (enrolId == null || enrolId.isEmpty()) {
            logger.error("Failed to get the enrolid from the config");
            throw new NodeProcessException("Failed to get the enrolid from the config");
        }
        
        logger.error("Enrolid: " + enrolId);
        
        logger.error("Get the audiosource from the shared state: ");
        List<String> audioSource = sharedState.get(VoisentryConstants.AUDIOSOURCE).asList(String.class);
        if (audioSource == null || audioSource.isEmpty()) {
            throw new NodeProcessException("Audio sample not provided");
        }
        
        VoisentryRequest vsRequest = new VoisentryRequest(voisentryNodeUrl,
                                                          voisentryDatasetKey);
        VoisentryResponseUpdate vsResponse = vsRequest.sendUpdate(enrolId, audioSource);
        logger.error("Send update result: " + vsResponse.getResult());
        
        int responseCode = vsResponse.getStatus();
        if (responseCode == VoisentryResponseCode.ALL_GOOD) {
            //check the verification result
            logger.error("Update successful");
            return goTo(true).build();
                
        } else {
            
            logger.error("Update failure: Error: " + responseCode);
            
            for (VoisentryErrorCode errorCode : config.errorCodeOutcomes()) {
                if (errorCode.getCode() == responseCode) {
                    return goTo("Err"+errorCode.getCode()).build();
                }
            }
            
        }
        
        return goTo("false").build();
                
    }
    
    
    private Action.ActionBuilder goTo(String outcome) {
        return Action.goTo(outcome);
    }
    
    public static class VoisentryUpdateNodeOutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        private static final String BUNDLE = VoisentryVerifyNode.class.getName().replace(".", "/");
        
        private static final Logger logger = LoggerFactory.getLogger(VoisentryVerifyNode.class);

        @Override
        public List<org.forgerock.openam.auth.node.api.OutcomeProvider.Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, org.forgerock.openam.auth.node.api.OutcomeProvider.class.getClassLoader());
            
            List<org.forgerock.openam.auth.node.api.OutcomeProvider.Outcome> outcomes = Arrays.asList(new org.forgerock.openam.auth.node.api.OutcomeProvider.Outcome("true",  bundle.getString("True")),
                                                   new org.forgerock.openam.auth.node.api.OutcomeProvider.Outcome("false", bundle.getString("False")));
            
            try {
                logger.error("Getting the error outcomes list");
                List<org.forgerock.openam.auth.node.api.OutcomeProvider.Outcome> errorOutcomes = nodeAttributes.get("errorCodeOutcomes").required()
                                                            .asList(String.class)
                                                            .stream()
                                                            .map(outcome -> new org.forgerock.openam.auth.node.api.OutcomeProvider.Outcome(outcome, outcome.replace("Err", "Error ")))
                                                            .collect(Collectors.toList());
                
                logger.error("Adding the error outcomes list to the node outcome: " + errorOutcomes.toString());
                errorOutcomes.add(0, new org.forgerock.openam.auth.node.api.OutcomeProvider.Outcome("false", bundle.getString("False")));
                errorOutcomes.add(0, new org.forgerock.openam.auth.node.api.OutcomeProvider.Outcome("true",  bundle.getString("True")));
                
                return errorOutcomes;
            } catch (JsonValueException e) {
                logger.error("Outcome exception: " + e.getMessage());
            }
            logger.error("Returning outcomes");
            return outcomes;
            
        }
        
    }
    
}
