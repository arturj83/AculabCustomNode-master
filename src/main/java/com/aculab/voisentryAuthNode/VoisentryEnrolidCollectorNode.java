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


package com.aculab.voisentryAuthNode;

import com.google.common.base.Strings;
import static org.forgerock.openam.auth.node.api.Action.send;

import javax.inject.Inject;
 
import org.forgerock.openam.auth.node.api.SingleOutcomeNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.forgerock.json.JsonValue;

import com.google.inject.assistedinject.Assisted;

import java.util.ResourceBundle;
import javax.security.auth.callback.NameCallback;


//TODO Update javadoc
/**
 * A node that that collects the enroll id and saves it in the shared state.
 * @author artur.jablonski@aculab.com
 */
@Node.Metadata(outcomeProvider  = SingleOutcomeNode.OutcomeProvider.class,
               configClass      = VoisentryEnrolidCollectorNode.Config.class)
public class VoisentryEnrolidCollectorNode extends SingleOutcomeNode {

    private final Logger logger = LoggerFactory.getLogger(VoisentryEnrolidCollectorNode.class);

    /**
     * Configuration for the node.
     */
    public interface Config {
    }
    
    private static final String BUNDLE = "com/example/customAuthNode/VoisentryEnrolidCollectorNode";


    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     */
    @Inject
    public VoisentryEnrolidCollectorNode(@Assisted Config config) {
    }

    @Override
    public Action process(TreeContext context) {
        
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
