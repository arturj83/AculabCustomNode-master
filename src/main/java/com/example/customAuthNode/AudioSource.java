/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.customAuthNode;

import java.io.Serializable;

/**
 * @author hollowek
 */
//TODO Not Used
public class AudioSource implements Serializable {

    public static final byte SRC_TYPE_FILE = 0;
    public static final byte SRC_TYPE_URL = 1;

    private final byte srcType;   /* file, url */
    private final String srcData;

    //TODO Not Used
    public AudioSource(String srcData, byte srcType) {

        this.srcData = srcData;
        this.srcType = srcType;

    }

    //TODO Not used
    public String getSrcData() {
        return this.srcData;
    }

    public byte getSrcType() {
        return this.srcType;
    }

    //TODO Not used
    public String getTypeString() {

        if (this.srcType == AudioSource.SRC_TYPE_FILE) {
            return "source";
        }

        if (this.srcType == AudioSource.SRC_TYPE_URL) {
            return "url";
        }

        return "";

    }

}
