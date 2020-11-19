/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aculab.voisentryAuthNode;

import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to process Update response, inherits from the VoisentryResponse class.
 * @author artur.jablonski@aculab.com
 */
public class VoisentryResponseUpdate extends VoisentryResponse {


    public VoisentryResponseUpdate (String response) throws NodeProcessException {
        
        super(response);
        
        if (this.status != VoisentryResponseCode.ALL_GOOD) {
            return;
        }

        //TODO What is the purpose of this try statement? - Artur: To catch tje json exception
        //we may change something in how we present the update reult from voisentry
        try {

            JSONObject jsonResult = new JSONObject(this.result);
            JSONObject jsonUpdate = new JSONObject(jsonResult.getString(VoisentryResponseConstants.UPDATED));

        } catch (JSONException e) {
            Logger logger = LoggerFactory.getLogger(VoisentryResponseUpdate.class);
            logger.error("Exception message: " + e.getMessage());
            throw new NodeProcessException(e);
        }
           
    }
    
    @Override
    public int getStatus() {
        return this.status;
    }
    
}