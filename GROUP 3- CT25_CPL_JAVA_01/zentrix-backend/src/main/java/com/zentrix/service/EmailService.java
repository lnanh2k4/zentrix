package com.zentrix.service;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  February 18, 2025
 */

public interface EmailService {
    /**
     * This method allows to send email with token
     * 
     * @param userEmail email of user
     * @param link      path in order to verify email
     */
    void sendEmailWithTokenForVerifyAccount(String userEmail, String link);

    void sendEmailWithTokenForResetPassword(String userEmail, String link);

    void sendEmailWithHtmlBody(String to, String cc, String bcc, String subject, String body);

    void sendEmailWithoutHtmlBody(String to, String cc, String bcc, String subject, String body);

    void sendEmailWithHtmlBodyAndFilePath(String to, String cc, String bcc, String subject, String body,
            String filePath);
}
