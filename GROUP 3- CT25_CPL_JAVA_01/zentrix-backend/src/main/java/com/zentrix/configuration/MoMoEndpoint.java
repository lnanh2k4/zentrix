package com.zentrix.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 26, 2025
 */
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MoMoEndpoint {
    String endpoint;
    String create;

    public String getCreateUrl() {
        return endpoint + create;
    }

}
