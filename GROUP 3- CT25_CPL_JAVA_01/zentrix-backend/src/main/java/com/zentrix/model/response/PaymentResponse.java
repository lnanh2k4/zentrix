package com.zentrix.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 26, 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse extends MomoResponse {

    public PaymentResponse(Integer resultCode, String message) {
        this.resultCode = resultCode;
        this.message = message;
    }

    private String requestId;

    private Long amount;

    private String payUrl;

    private String shortLink;

    private String deeplink;

    private String qrCodeUrl;

    private String deeplinkWebInApp;

    private Long transId;

    private String applink;

    private String partnerClientId;

    private String bindingUrl;

    private String deeplinkMiniApp;
}