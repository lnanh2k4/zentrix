package com.zentrix.model.utils;

import com.google.gson.annotations.SerializedName;

public enum RequestType {
    /*
     * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
     * 
     * @date February 26, 2025
     */
    /**
     * The capture momo wallet.
     */
    @SerializedName("captureWallet")
    CAPTURE_WALLET("captureWallet"),

    /**
     * The pay with atm
     */
    @SerializedName("payWithATM")
    PAY_WITH_ATM("payWithATM"),

    /**
     * The pay with method
     */
    @SerializedName("payWithMethod")
    PAY_WITH_METHOD("payWithMethod"),

    /**
     * The pay with credit Card
     */
    @SerializedName("payWithCC")
    PAY_WITH_CREDIT("payWithCC"),

    /**
     * The link momo wallet and pay momo wallet if amount > 0.
     */
    @SerializedName("linkWallet")
    LINK_WALLET("linkWallet");

    private final String value;

    RequestType(String value) {
        this.value = value;
    }

    public static RequestType findByName(String name) {
        for (RequestType type : values()) {
            if (type.getRequestType().equals(name)) {
                return type;
            }
        }
        return null;
    }

    public String getRequestType() {
        return value;
    }
}
