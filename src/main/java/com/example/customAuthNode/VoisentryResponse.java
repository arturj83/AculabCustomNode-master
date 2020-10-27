/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.customAuthNode;

import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class used to process responses from Voisentry server.
 * @author artur.jablonski@aculab.com
 */
public class VoisentryResponse {
    
    protected int    status;
    protected String result;


    public VoisentryResponse(String response) throws NodeProcessException {

        this.result = null;

        try {
            JSONObject jsonResponse = new JSONObject(response);
            this.status = jsonResponse.getInt(VoisentryResponseConstants.STATUS);
            this.result = jsonResponse.getString(VoisentryResponseConstants.RESULT);
        } catch (JSONException e) {
            throw new NodeProcessException(e);
        }
    }
    
    public int getStatus() {
        return this.status;
    }
    
    public String getResult() {
        return this.result;
    }
    
}