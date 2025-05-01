package com.zentrix.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.Headers;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 26, 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpResponse {

    int status;
    String data;
    Headers headers;
}