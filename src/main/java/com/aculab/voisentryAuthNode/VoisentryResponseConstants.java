/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aculab.voisentryAuthNode;

/**
 * Voisentry response json values.
 * @author artur.jablonski@aculab.com
 */
public final class VoisentryResponseConstants {
    
    /**
     * Private constructor.
     */
    private VoisentryResponseConstants() {
    }
    
    /** The status code. */
    public static final String STATUS = "status";
    
    /** The result info. */
    public static final String RESULT = "result";
    
    /** The verification json array. */
    public static final String VERIFICATION = "verification";
    
    /** The confidence value - float. */
    public static final String CONFIDENCE = "confidence";
    
    /** The verified value - boolean. */
    public static final String VERIFIED = "verified";
    
    /** The PAD info json array. */
    public static final String PAD_INFO = "PAD_info";
    
    /** The PAD type. */
    public static final String PAD_TYPE = "type";
    
    /** The updated json array. */
    public static final String UPDATED = "updated";
    
}
