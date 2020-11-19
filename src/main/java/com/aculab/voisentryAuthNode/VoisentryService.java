/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aculab.voisentryAuthNode;

import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.annotations.sm.Config;

/**
 * Voisentry service configuration shared between the nodes
 * @author artur.jablonski@aculab.com
 */
@Config(scope = Config.Scope.REALM)
public interface VoisentryService {
    
    /**
    * The voisentry node URL used to receive the audio sample.
    */
    @Attribute(order = 10)
    String voisentryNodeUrl();
    
    /**
    * The voisentry dataset key used to store the voiceprints.
    */
    @Attribute(order = 20)
    String voisentryDatasetKey();
    
    /**
    * The voisentry dataset key used to store the voiceprints.
    */
    @Attribute(order = 30)
    default VoisentryConstants.ServiceGetEnrolId getEnrolId() { 
        return VoisentryConstants.ServiceGetEnrolId.USERNAME;
    }
    
    /**
    * The id repository field name for the enrollid source.
    */
    @Attribute(order = 40)
    String idRepoEnrolidName();

    
    
}
