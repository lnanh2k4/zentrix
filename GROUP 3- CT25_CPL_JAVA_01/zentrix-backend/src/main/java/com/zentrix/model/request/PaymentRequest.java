package com.zentrix.model.request;

import com.zentrix.model.utils.Language;
import com.zentrix.model.utils.RequestType;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 26, 2025
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequest extends MomoRequest {
    String orderInfo;

    long amount;
    String partnerName;
    String subPartnerCode;
    RequestType requestType;
    String redirectUrl;
    String ipnUrl;
    String storeId;
    String extraData;
    String partnerClientId;
    Boolean autoCapture = true;
    Long orderGroupId;
    String signature;

    public PaymentRequest(String partnerCode, String orderId, String requestId, Language lang, String orderInfo,
            long amount, String partnerName, String subPartnerCode, RequestType requestType, String redirectUrl,
            String ipnUrl, String storeId, String extraData, String partnerClientId, Boolean autoCapture,
            Long orderGroupId, String signature) {
        super(partnerCode, orderId, requestId, lang);
        this.orderInfo = orderInfo;
        this.amount = amount;
        this.partnerName = partnerName;
        this.subPartnerCode = subPartnerCode;
        this.requestType = requestType;
        this.redirectUrl = redirectUrl;
        this.ipnUrl = ipnUrl;
        this.storeId = storeId;
        this.extraData = extraData;
        this.partnerClientId = partnerClientId;
        this.autoCapture = autoCapture;
        this.orderGroupId = orderGroupId;
        this.signature = signature;
    }

}
