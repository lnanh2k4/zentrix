package com.zentrix.model.utils;

import com.google.gson.annotations.SerializedName;
import com.zentrix.constants.Constants;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 26, 2025
 */
public enum Language {

    @SerializedName("vi")
    VI(Constants.LANGUAGE_VI),

    @SerializedName("en")
    EN(Constants.LANGUAGE_EN);

    private final String value;

    Language(String value) {
        this.value = value;
    }

    public static Language findByName(String name) {
        for (Language type : values()) {
            if (type.getLanguage().equals(name)) {
                return type;
            }
        }
        return null;
    }

    public String getLanguage() {
        return value;
    }

}
