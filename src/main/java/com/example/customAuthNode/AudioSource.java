/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.customAuthNode;

import java.io.File;
import java.io.Serializable;

/**
 *
 * @author hollowek
 */
public class AudioSource implements Serializable {
    
    public static final byte SRC_TYPE_FILE = 0;
    public static final byte SRC_TYPE_URL  = 1;
    
    private byte        srcType;   /* file, url */
    //private final ContentBody srcData;
    private String      srcData;
    //private InputStream   srcData;
    //private File        srcFile;
    
    //public AudioSource (ContentBody srcData, byte srcType) {
    public AudioSource (String srcData, byte srcType) {
    //public AudioSource (InputStream srcData, byte srcType) {
    //public AudioSource (File srcData, byte srcType) {
        
        this.srcData = srcData;
        //this.srcFile = srcData;
        this.srcType = srcType;
        
    }
    
    /*public ContentBody getSrcData () {
        return this.srcData;
    }*/
    public String getSrcData () {
        return this.srcData;
    }
    /*public InputStream getSrcData () {
        return this.srcData;
    }*/
    /*public File getSrcData () {
        return this.srcFile;
    }*/
    
    public byte getSrcType () {
        return this.srcType;
    }
        
    public String getTypeString () {
        
        if (this.srcType == AudioSource.SRC_TYPE_FILE) {
            return "source";
        }
        
        if (this.srcType == AudioSource.SRC_TYPE_URL) {
            return "url";
        }
        
        return "";
        
    }
    
}
