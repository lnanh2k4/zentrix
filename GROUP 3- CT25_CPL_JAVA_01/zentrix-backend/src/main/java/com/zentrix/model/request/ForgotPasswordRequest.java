package com.zentrix.model.request;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String identifier;
    private String method;
}
