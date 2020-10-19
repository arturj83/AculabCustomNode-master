/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.customAuthNode;

/**
 *
 * @author hollowek
 */
public enum VoisentryErrorCode {
    
    /** the error codes. */

    //TODO Error codes never used
    Err400("Error 400 Bad API",          VoisentryResponseCode.ERR_BAD_API_REQ),
    Err405("Error 405 API not allowed",  VoisentryResponseCode.ERR_BAD_API_REQ_NOT_ALLOWED),
    Err408("Error 408 DB timeout",       VoisentryResponseCode.ERR_DB_TIMEOUT),
    Err429("Error 429 Quota exceeded",   VoisentryResponseCode.ERR_QUOTA),
    Err435("Error 435 Audio file",       VoisentryResponseCode.ERR_AUDIO_FILE),
    Err445("Error 445 Bad audio",        VoisentryResponseCode.ERR_AUDIO),
    Err500("Error 500 Exception",        VoisentryResponseCode.ERR_EXCEPTION),
    Err503("Error 503 Service disabled", VoisentryResponseCode.ERR_SERVICE),
    Err507("Error 507 Disk/Memory",      VoisentryResponseCode.ERR_DISK_MEM);

    private final String name;
    private final int errorCode;
    
    /**
     * The constructor.
     *
     * @param name the algorithm name.
     *
     */
    VoisentryErrorCode(String name, int errorCode) {
        this.name      = name;
        this.errorCode = errorCode;
    }
    
    
    /**
     * Get the name of the error.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }
    
    
    /**
     * Get the code of the error.
     *
     * @return the name.
     */
    public int getCode() {
        return errorCode;
    }
    
}
