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

package com.example.customAuthNode;

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
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdUtils;
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
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;
import org.forgerock.openam.sm.AnnotatedServiceRegistry;
import org.forgerock.util.i18n.PreferredLocales;


/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
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
        
        logger.error("Get config attributes");
        String voisentryNodeUrl    = config.voisentryNodeUrl();
        if (voisentryNodeUrl == null || voisentryNodeUrl.isEmpty()) {
            voisentryNodeUrl = serviceConfig.voisentryNodeUrl();
        }
        if (voisentryNodeUrl == null || voisentryNodeUrl.isEmpty()) {
            throw new NodeProcessException("Failed to get voisentry node URL from the config");
        }
        logger.error("Voisentry node URL: " + voisentryNodeUrl);
        
        
        String voisentryDatasetKey = config.voisentryDatasetKey();
        if (voisentryDatasetKey == null || voisentryDatasetKey.isEmpty()) {
            voisentryDatasetKey = serviceConfig.voisentryDatasetKey();
        }
        if (voisentryDatasetKey == null || voisentryDatasetKey.isEmpty()) {
            throw new NodeProcessException("Failed to get voisentry dataset key from the config");
        }
        logger.error("Voisentry dataset key: " + voisentryDatasetKey);
        
        logger.error("Get the shared state");
        JsonValue sharedState = context.sharedState;
        
        String enrolId = null;
        
        if (config.getEnrolId() == VoisentryConstants.GetEnrolId.SERVICE) {
            
            VoisentryConstants.ServiceGetEnrolId configGetEnrolId  = serviceConfig.getEnrolId();
            
            if (configGetEnrolId == VoisentryConstants.ServiceGetEnrolId.ENROLID) {
            
                logger.error("Get the enrolid from the shared state");
                String enrolid = null;
                try {
                    enrolid = sharedState.get(VoisentryConstants.ENROLID).asString();
                } catch (Exception e) {
                    logger.error("Get enrolid exception: " + e.getMessage());
                    throw new NodeProcessException(e);
                }
                if (enrolid == null || enrolid.isEmpty()) {
                    logger.error("EnrolId not provided");
                    throw new NodeProcessException("EnrolId not provided");
                }
            
                enrolId = enrolid;
            
            } else {
            
                logger.error("Get the username from the shared state");
                String username = null;
                try {
                    username = sharedState.get(USERNAME).asString();
                    logger.error("Username: " + username);
                } catch (Exception e) {
                    logger.error("Get username exception: " + e.getMessage());
                    throw new NodeProcessException(e);
                }
                if (username == null || username.isEmpty()) {
                    logger.error("Username not provided");
                    throw new NodeProcessException("Username not provided");
                }
            
                if (configGetEnrolId == VoisentryConstants.ServiceGetEnrolId.USERNAME) {
                    enrolId = username;
                } else if (configGetEnrolId == VoisentryConstants.ServiceGetEnrolId.ID_REPO) {
                
                    //Code to get the employee Number
                    try {
                        logger.error("Get enrolid from the id repo");
                        String idRepoEnrolid = serviceConfig.idRepoEnrolidName();
                        if (idRepoEnrolid == null || idRepoEnrolid.isEmpty()) {
                            throw new NodeProcessException("Id repository enrol id field name not provided");
                        }
                        String idRepoEnrolId = IdUtils.getIdentity(username, realm).getAttribute(idRepoEnrolid).iterator()
                                                      .next();
                        enrolId = idRepoEnrolId;
                    } catch (IdRepoException | SSOException e) {
                        logger.error("Get id repo exception: " + e.getMessage());
                        throw new NodeProcessException(e);
                    }
                }
            }
        }
        
        if (config.getEnrolId() == VoisentryConstants.GetEnrolId.ENROLID) {
            
            logger.error("Get the enrolid from the shared state");
            String enrolid = null;
            try {
                enrolid = sharedState.get(VoisentryConstants.ENROLID).asString();
            } catch (Exception e) {
                logger.error("Get enrolid exception: " + e.getMessage());
                throw new NodeProcessException(e);
            }
            
            enrolId = enrolid;
            
        } else {
            
            VoisentryConstants.GetEnrolId configGetEnrolId  = config.getEnrolId();
            String                        configEnrolIdName = config.idRepoEnrolidName();
            
            logger.error("Get the username from the shared state");
            String username = null;
            try {
                username = sharedState.get(USERNAME).asString();
                logger.error("Username: " + username);
            } catch (Exception e) {
                logger.error("Get username exception: " + e.getMessage());
                throw new NodeProcessException(e);
            }
            if (username == null || username.isEmpty()) {
                logger.error("Username not provided");
                throw new NodeProcessException("Username not provided");
            }
            
            if (configGetEnrolId == VoisentryConstants.GetEnrolId.USERNAME) {
                enrolId = username;
            } else if (configGetEnrolId == VoisentryConstants.GetEnrolId.ID_REPO) {
                
                //Code to get the employee Number
                try {
                    logger.error("Get enrolid from the id repo");
                    String idRepoEnrolid = config.idRepoEnrolidName();
                    if (idRepoEnrolid == null || idRepoEnrolid.isEmpty()) {
                        throw new NodeProcessException("Id repository enrol id field name not provided");
                    }
                    String idRepoEnrolId = IdUtils.getIdentity(username, realm).getAttribute(idRepoEnrolid).iterator()
                                                   .next();
                    enrolId = idRepoEnrolId;
                } catch (IdRepoException | SSOException e) {
                    logger.error("Get id repo exception: " + e.getMessage());
                    throw new NodeProcessException(e);
                }
            }
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
