/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aculab.voisentryAuthNode;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Class used to send http requests to Voisentry server.
 * @author artur.jablonski@aculab.com
 */
public class VoisentryRequest {
    
    private final Logger logger = LoggerFactory.getLogger(VoisentryRequest.class);
    
    private final String voisentryNodeUrl;
    private final String voisentryDatasetKey;


    public VoisentryRequest(String voisentryNodeUrl, String voisentryDatasetKey) {
        this.voisentryNodeUrl    = voisentryNodeUrl;
        this.voisentryDatasetKey = voisentryDatasetKey;
    }

    //send verify request
    public VoisentryResponseVerify sendVerify(String enrolId, List<String> audioSource) throws NodeProcessException {
        
        logger.error("Sending verify request...");
        
        String reqUrl = this.voisentryNodeUrl + "/verify?key=" + this.voisentryDatasetKey + "&enrolid=" + enrolId;
        
        String result = this.sendPostRequest(reqUrl, createMpartBody(audioSource));
        logger.error("Send verify post request result: " + result);
        
        
        return (new VoisentryResponseVerify(result));
        
    }

    //send update request
    public VoisentryResponseUpdate sendUpdate(String enrolId, List<String> audioSource) throws NodeProcessException {
        
        logger.error("Sending update request...");
        
        String reqUrl = this.voisentryNodeUrl + "/update?key=" + this.voisentryDatasetKey + "&enrolid=" + enrolId;
        
        String result = this.sendPostRequest(reqUrl, createMpartBody(audioSource));
        logger.error("Send update post request result: " + result);
        
        
        return (new VoisentryResponseUpdate(result));
        
    }

    //send post request
    private String sendPostRequest(String url, MultipartEntityBuilder mpart) throws NodeProcessException {

        try {

            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

                HttpPost httppost = new HttpPost(url);

                HttpEntity reqEntity = mpart.build();
                httppost.setEntity(reqEntity);

                logger.error("executing request " + httppost.getRequestLine());
                try (CloseableHttpResponse response = httpclient.execute(httppost)) {

                    HttpEntity resEntity = response.getEntity();
                    if (resEntity != null) {
                        return EntityUtils.toString(response.getEntity());
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new NodeProcessException(e);
        }
        return null;

    }
    
    
    //create multipart body
    private MultipartEntityBuilder createMpartBody (List<String> audioSource) {
        
        MultipartEntityBuilder mpart = MultipartEntityBuilder.create();
        if (audioSource != null && !audioSource.isEmpty()) {
            for (int i = 0; i < audioSource.size(); i++) {
                String argName = "source";
                if (audioSource.size() > 1) {
                    argName += "" + (i+1);
                }
                mpart.addBinaryBody(argName,
                                    Base64.getDecoder().decode(audioSource.get(i)),
                                    ContentType.DEFAULT_BINARY,
                                    "audiosource" + (i+1) + ".wav");
                
            }
 
        }
        
        return mpart;
        
    }
    
}
