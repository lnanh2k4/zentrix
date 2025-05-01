package com.zentrix.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 26, 2025
 */
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartnerInfo {

    String accessKey;
    String partnerCode;
    String secretKey;
    String publicKey;

    public PartnerInfo(String partnerCode, String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.partnerCode = partnerCode;
        this.secretKey = secretKey;
    }

}
