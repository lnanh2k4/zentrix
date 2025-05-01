package com.zentrix.model.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String token;
    private String otp;
    private String newPassword;
}
