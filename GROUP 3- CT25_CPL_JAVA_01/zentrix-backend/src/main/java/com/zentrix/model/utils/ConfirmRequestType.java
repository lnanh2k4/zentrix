package com.zentrix.model.utils;

import com.google.gson.annotations.SerializedName;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 26, 2025
 */
public enum ConfirmRequestType {

    /**
     * The capture momo wallet.
     */
    @SerializedName("capture")
    CAPTURE("capture"),

    /**
     * The pay with atm
     */
    @SerializedName("cancel")
    CANCEL("cancel");

    private final String value;

    ConfirmRequestType(String value) {
        this.value = value;
    }

    public static ConfirmRequestType findByName(String name) {
        for (ConfirmRequestType type : values()) {
            if (type.getConfirmRequestType().equals(name)) {
                return type;
            }
        }
        return null;
    }

    public String getConfirmRequestType() {
        return value;
    }
}