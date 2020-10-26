/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.customAuthNode;

import com.sun.identity.shared.validation.URLValidator;
import com.sun.identity.sm.RequiredValueValidator;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.annotations.sm.Config;

/**
 *
 * @author hollowek
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
    * The id repository field name for the enrollid source
    */
    @Attribute(order = 40)
    String idRepoEnrolidName();

    
    
}
