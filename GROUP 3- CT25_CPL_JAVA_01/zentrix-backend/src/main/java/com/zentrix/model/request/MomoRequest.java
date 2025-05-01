package com.zentrix.model.request;

import com.zentrix.model.utils.Language;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 26, 2025
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MomoRequest {
    String partnerCode;
    String requestId;
    String orderId;
    Language lang = Language.EN;
    long startTime;

    public MomoRequest() {
        this.startTime = System.currentTimeMillis();
    }

    public MomoRequest(String partnerCode, String orderId, String requestId, Language lang) {
        this.partnerCode = partnerCode;
        this.orderId = orderId;
        this.requestId = requestId;
        this.lang = lang;
        this.startTime = System.currentTimeMillis();
    }

}
