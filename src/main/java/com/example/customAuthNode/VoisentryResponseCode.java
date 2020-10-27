/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.customAuthNode;

/**
 * Response codes from Voisentry server.
 * @author artur.jablonski@aculab.com
 */
public final class VoisentryResponseCode {
    
    /**
     * Private constructor.
     */
    private VoisentryResponseCode() {
    }
    
    /** Bad API request - (the service has been used incorrectly. */
    public static final int ALL_GOOD                    = 200;
    
    /** Bad API request - (the service has been used incorrectly. */
    public static final int ERR_BAD_API_REQ             = 400;
    
    /** Bad API request - method not allowed. */
    public static final int ERR_BAD_API_REQ_NOT_ALLOWED = 405;
    
    /** Database timeout error or Database access error. */
    public static final int ERR_DB_TIMEOUT              = 408;
    
    /** Several possible descriptions:
      * System database volume too full for enrolments or updates.
      • Cluster enrolment quota exceeded.
      • Tenant enrolment quota exceeded.
      • Accesskey enrolment quota exceeded. */
    public static final int ERR_QUOTA                   = 429;
    
    /** Has several possible descriptions:
      • The audio file contains no data.
      • The audio is in an unknown format.
      • The sample rate is too low (below 8 kHz).
      • The bit depth is too low (less than 16 and not A-Law or mu-Law).
      • The audio duration is too long (more than 60 seconds).
      • The audio is not single-channel (mono). */
    public static final int ERR_AUDIO_FILE              = 435;
    
    /** - Insufficient speaker observations in the audio source. */
    public static final int ERR_AUDIO                   = 445;
    
    /** - An exception has occurred. */
    public static final int ERR_EXCEPTION               = 500;
    
    /** - Service temporarily disabled. */
    public static final int ERR_SERVICE                 = 503;
    
    /** - - Not enough disk space or memory to fulfill the task. */
    public static final int ERR_DISK_MEM                = 507;
    
}
