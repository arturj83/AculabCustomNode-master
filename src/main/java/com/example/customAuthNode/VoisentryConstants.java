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
public final class VoisentryConstants {
    
    /**
     * Private constructor.
     */
    private VoisentryConstants() {
    }
    
    /** The enrolId. */
    public static final String ENROLID = "voisentry_enrolid";
    
    /** The audiosource. */
    public static final String AUDIOSOURCE = "voisentry_audiosource";
    
    /** Config - enrolid source. */
    public enum GetEnrolId {
        SERVICE,
        USERNAME,
        ENROLID,
        ID_REPO
    }
    
    /** Config - enrolid source. */
    public enum ServiceGetEnrolId {
        USERNAME,
        ENROLID,
        ID_REPO
    }
    
}
