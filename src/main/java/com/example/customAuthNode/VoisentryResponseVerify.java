/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.customAuthNode;

import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hollowek
 */
public class VoisentryResponseVerify extends VoisentryResponse {
    
    private final Logger logger = LoggerFactory.getLogger(VoisentryResponseVerify.class);
    
    private double     confidence;
    private boolean    verified;
    
    private String     pad_type;
    
    
    public VoisentryResponseVerify (String response) throws NodeProcessException {
        
        super(response);
        
        this.pad_type = null;
        
        if (this.status != VoisentryResponseCode.ALL_GOOD) {
            return;
        }
        
        try {
        
            JSONObject jsonResult = new JSONObject(this.result);
            JSONObject jsonVerify = new JSONObject(jsonResult.getString(VoisentryResponseConstants.VERIFICATION));
            
            this.confidence = jsonVerify.getDouble (VoisentryResponseConstants.CONFIDENCE);
            this.verified   = jsonVerify.getBoolean(VoisentryResponseConstants.VERIFIED);
            
            JSONObject pad_info = new JSONObject(jsonVerify.getString(VoisentryResponseConstants.PAD_INFO));
            if (pad_info.has(VoisentryResponseConstants.PAD_TYPE)) {
                logger.error("Has pad_type");
                this.pad_type = pad_info.getString(VoisentryResponseConstants.PAD_TYPE);
            }
    
        } catch (JSONException e) {
            logger.error("Exception message: " + e.getMessage());
            throw new NodeProcessException(e);
        }
           
    }
    
    public     int getStatus() {
        return this.status;
    }
    
    public  double getConfidence() {
        return this.confidence;
    }
    
    public boolean getVerified() {
        return this.verified;
    }
    
    public  String getPadType() {
        return this.pad_type;
    }
    
}