package com.zentrix.service;

import java.io.File;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  February 18, 2025
 */
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {

    JavaMailSender javaMailSender;

    @Override
    public void sendEmailWithTokenForResetPassword(String to, String token) {
        String resetUrl = "http://localhost:5173/forgot-password/reset-password?token=" + token;
        // Create mime message
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        // Init subject and text into String pool
        String subject = "[ZENTRIX STORE] RESET YOUR PASSWORD";
        String text = "<!DOCTYPE html>\r\n" + //
                "<html lang=\"en\">\r\n" + //
                "\r\n" + //
                "<head>\r\n" + //
                "    <meta charset=\"UTF-8\">\r\n" + //
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + //
                "    <title>Reset Your Password</title>\r\n" + //
                "    <style>\r\n" + //
                "        body {\r\n" + //
                "            font-family: Arial, sans-serif;\r\n" + //
                "            margin: 0;\r\n" + //
                "            padding: 0;\r\n" + //
                "            background-color: #f4f4f4;\r\n" + //
                "            color: #333;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .container {\r\n" + //
                "            max-width: 600px;\r\n" + //
                "            margin: 20px auto;\r\n" + //
                "            background-color: #ffffff;\r\n" + //
                "            padding: 20px;\r\n" + //
                "            border-radius: 8px;\r\n" + //
                "            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .header {\r\n" + //
                "            text-align: center;\r\n" + //
                "            padding-bottom: 20px;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .header img {\r\n" + //
                "            max-width: 150px;\r\n" + //
                "            height: auto;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .header .store-name {\r\n" + //
                "            font-size: 24px;\r\n" + //
                "            font-weight: bold;\r\n" + //
                "            color: #007bff;\r\n" + //
                "            margin-top: 10px;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .content {\r\n" + //
                "            font-size: 16px;\r\n" + //
                "            line-height: 1.5;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .button {\r\n" + //
                "            display: inline-block;\r\n" + //
                "            padding: 12px 24px;\r\n" + //
                "            text-decoration: none;\r\n" + //
                "            border-radius: 5px;\r\n" + //
                "            font-weight: bold;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .footer {\r\n" + //
                "            margin-top: 20px;\r\n" + //
                "            font-size: 12px;\r\n" + //
                "            color: #777;\r\n" + //
                "            text-align: center;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        @media only screen and (max-width: 600px) {\r\n" + //
                "            .container {\r\n" + //
                "                padding: 15px;\r\n" + //
                "            }\r\n" + //
                "\r\n" + //
                "            .header img {\r\n" + //
                "                max-width: 120px;\r\n" + //
                "            }\r\n" + //
                "\r\n" + //
                "            .header .store-name {\r\n" + //
                "                font-size: 20px;\r\n" + //
                "            }\r\n" + //
                "\r\n" + //
                "            .button {\r\n" + //
                "                display: block;\r\n" + //
                "                text-align: center;\r\n" + //
                "            }\r\n" + //
                "        }\r\n" + //
                "    </style>\r\n" + //
                "</head>\r\n" + //
                "\r\n" + //
                "<body>\r\n" + //
                "    <div class=\"container\">\r\n" + //
                "        <div class=\"header\">\r\n" + //
                "            <img src=\"https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiFiXwQ_maatzhJ8oajcgfHJc1mpwv1UDv_pWLFT53HHtUaRZkvOQcuhofX5tamuITa9dxqKQI5wJQ3e3wNqnn1K7sxwdMjGJHY8E_BLsizh8quZdyWs9SX0elWyXJN3Aps-bxHI0wvGvahJapqjJaBJBPXvg5fHqewoiz0RU7ApdukKvi9kbdbSxzPtbqI/s1628/logo%20new%20(1).png\"\r\n"
                + //
                "                alt=\"Zentrix Logo\">\r\n" + //
                "\r\n" + //
                "            <div class=\"store-name\">Zentrix Store</div>\r\n" + //
                "        </div>\r\n" + //
                "        <div class=\"content\">\r\n" + //
                "            <h2>Reset Your Password</h2>\r\n" + //
                "            <p>Hello,</p>\r\n" + //
                "            <p>We received a request to reset your password. Click the link below to reset it:</p>\r\n"
                + //
                "\r\n" + //
                "            <p>\r\n" + //
                "            <div>\r\n" + //
                "                <a href=" + resetUrl
                + " class=\"button\" style=\"color: white; background-color: rgb(29, 68, 196);\">Reset\r\n"
                + //
                "                    Password</a>\r\n" + //
                "            </div>\r\n" + //
                "            </p>\r\n" + //
                "            <p>This link will expire in 15 minutes for your security.</p>\r\n" + //
                "            <p>If you didn’t request a password reset, please ignore this email or contact our support team.</p>\r\n"
                + //
                "\r\n" + //
                "            <p>Best regards,<br>The Zentrix Team</p>\r\n" + //
                "        </div>\r\n" + //
                "        <div class=\"footer\">\r\n" + //
                "            <p>© " + java.time.Year.now().getValue() + " Zentrix. All rights reserved.</p>\r\n" + //
                "            <p>Need help? Contact us at <a href=\"mailto:zentrix.system@gmail.com\">zentrix.system@gmail.com</a></p>\r\n"
                + //
                "        </div>\r\n" + //
                "    </div>\r\n" + //
                "</body>\r\n" + //
                "\r\n" + //
                "</html>";

        try {
            // Init mime message helper so that supports easier configure
            // config utf-8 so that support Vietnamese language
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            // Config Subject, Text and Sender
            helper.setSubject(subject);
            helper.setText(text, true);
            helper.setTo(to);
            // Send email
            javaMailSender.send(mimeMessage);
        } catch (MailSendException e) {
            // Throw exception if it is failed during sending email
            throw new ActionFailedException(AppCode.EMAIL_SENT_FAILED, e.getCause());
        } catch (MessagingException e) {
            // Throw exception if it is failed during setting email
            throw new ActionFailedException(AppCode.MESSAGE_SETED_FAILED, e.getCause());
        }

    }

    @Override
    public void sendEmailWithTokenForVerifyAccount(String to, String token) {
        String verifyUrl = "http://localhost:5173/email/verified?token=" + token;
        // Create mime message
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        // Init subject and text into String pool
        String subject = "[ZENTRIX STORE] VERIFY EMAIL";
        String text = "<!DOCTYPE html>\r\n" + //
                "<html lang=\"en\">\r\n" + //
                "\r\n" + //
                "<head>\r\n" + //
                "    <meta charset=\"UTF-8\">\r\n" + //
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + //
                "    <title>Verify Email</title>\r\n" + //
                "    <style>\r\n" + //
                "        body {\r\n" + //
                "            font-family: Arial, sans-serif;\r\n" + //
                "            margin: 0;\r\n" + //
                "            padding: 0;\r\n" + //
                "            background-color: #f4f4f4;\r\n" + //
                "            color: #333;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .container {\r\n" + //
                "            max-width: 600px;\r\n" + //
                "            margin: 20px auto;\r\n" + //
                "            background-color: #ffffff;\r\n" + //
                "            padding: 20px;\r\n" + //
                "            border-radius: 8px;\r\n" + //
                "            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .header {\r\n" + //
                "            text-align: center;\r\n" + //
                "            padding-bottom: 20px;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .header img {\r\n" + //
                "            max-width: 150px;\r\n" + //
                "            height: auto;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .header .store-name {\r\n" + //
                "            font-size: 24px;\r\n" + //
                "            font-weight: bold;\r\n" + //
                "            color: #007bff;\r\n" + //
                "            margin-top: 10px;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .content {\r\n" + //
                "            font-size: 16px;\r\n" + //
                "            line-height: 1.5;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .button {\r\n" + //
                "            display: inline-block;\r\n" + //
                "            padding: 12px 24px;\r\n" + //
                "            text-decoration: none;\r\n" + //
                "            border-radius: 5px;\r\n" + //
                "            font-weight: bold;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        .footer {\r\n" + //
                "            margin-top: 20px;\r\n" + //
                "            font-size: 12px;\r\n" + //
                "            color: #777;\r\n" + //
                "            text-align: center;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        @media only screen and (max-width: 600px) {\r\n" + //
                "            .container {\r\n" + //
                "                padding: 15px;\r\n" + //
                "            }\r\n" + //
                "\r\n" + //
                "            .header img {\r\n" + //
                "                max-width: 120px;\r\n" + //
                "            }\r\n" + //
                "\r\n" + //
                "            .header .store-name {\r\n" + //
                "                font-size: 20px;\r\n" + //
                "            }\r\n" + //
                "\r\n" + //
                "            .button {\r\n" + //
                "                display: block;\r\n" + //
                "                text-align: center;\r\n" + //
                "            }\r\n" + //
                "        }\r\n" + //
                "    </style>\r\n" + //
                "</head>\r\n" + //
                "\r\n" + //
                "<body>\r\n" + //
                "    <div class=\"container\">\r\n" + //
                "        <div class=\"header\">\r\n" + //
                "            <img src=\"https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiFiXwQ_maatzhJ8oajcgfHJc1mpwv1UDv_pWLFT53HHtUaRZkvOQcuhofX5tamuITa9dxqKQI5wJQ3e3wNqnn1K7sxwdMjGJHY8E_BLsizh8quZdyWs9SX0elWyXJN3Aps-bxHI0wvGvahJapqjJaBJBPXvg5fHqewoiz0RU7ApdukKvi9kbdbSxzPtbqI/s1628/logo%20new%20(1).png\"\r\n"
                + //
                "                alt=\"Zentrix Logo\">\r\n" + //
                "\r\n" + //
                "            <div class=\"store-name\">Zentrix Store</div>\r\n" + //
                "        </div>\r\n" + //
                "        <div class=\"content\">\r\n" + //
                "            <h2>Verify Your Email</h2>\r\n" + //
                "            <p>Hello,</p>\r\n" + //
                "            <p>We received a request to reset your password. Click the link below to reset it:</p>\r\n"
                + //
                "\r\n" + //
                "            <p>\r\n" + //
                "            <div>\r\n" + //
                "                <a href=" + verifyUrl
                + " class=\"button\" style=\"color: white; background-color: rgb(29, 68, 196);\">VERIFY\r\n"
                + //
                "                    ACCOUNT</a>\r\n" + //
                "            </div>\r\n" + //
                "            </p>\r\n" + //
                "            <p>This link will expire in 15 minutes for your security.</p>\r\n" + //
                "            <p>If you didn’t request a verify account, please ignore this email or contact our support team.</p>\r\n"
                + //
                "\r\n" + //
                "            <p>Best regards,<br>The Zentrix Team</p>\r\n" + //
                "        </div>\r\n" + //
                "        <div class=\"footer\">\r\n" + //
                "            <p>© " + java.time.Year.now().getValue() + " Zentrix. All rights reserved.</p>\r\n" + //
                "            <p>Need help? Contact us at <a href=\"mailto:zentrix.system@gmail.com\">zentrix.system@gmail.com</a></p>\r\n"
                + //
                "        </div>\r\n" + //
                "    </div>\r\n" + //
                "</body>\r\n" + //
                "\r\n" + //
                "</html>";

        try {
            // Init mime message helper so that supports easier configure
            // config utf-8 so that support Vietnamese language
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            // Config Subject, Text and Sender
            helper.setSubject(subject);
            helper.setText(text, true);
            helper.setTo(to);
            // Send email
            javaMailSender.send(mimeMessage);
        } catch (MailSendException e) {
            // Throw exception if it is failed during sending email
            throw new ActionFailedException(AppCode.MESSAGE_SETED_FAILED, e.getCause());
        } catch (MessagingException e) {
            // Throw exception if it is failed during setting email
            throw new ActionFailedException(AppCode.MESSAGE_SETED_FAILED, e.getCause());
        }

    }

    @Override
    public void sendEmailWithHtmlBody(String to, String cc, String bcc, String subject, String body) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            // Config Subject, Text and Sender
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setTo(to);
            if (cc != null) {
                helper.setCc(cc);
            }
            if (bcc != null) {
                helper.setBcc(bcc);
            }
            // Send email
            javaMailSender.send(mimeMessage);
        } catch (MailSendException e) {
            // Throw exception if it is failed during sending email
            throw new ActionFailedException(AppCode.MESSAGE_SETED_FAILED, e.getCause());
        } catch (MessagingException e) {
            // Throw exception if it is failed during setting email
            throw new ActionFailedException(AppCode.MESSAGE_SETED_FAILED, e.getCause());
        }

    }

    @Override
    public void sendEmailWithoutHtmlBody(String to, String cc, String bcc, String subject, String body) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            // Config Subject, Text and Sender
            helper.setSubject(subject);
            helper.setText(body, false);
            helper.setTo(to);
            if (cc != null) {
                helper.setCc(cc);
            }
            if (bcc != null) {
                helper.setBcc(bcc);
            }

            // Send email
            javaMailSender.send(mimeMessage);
        } catch (MailSendException e) {
            // Throw exception if it is failed during sending email
            throw new ActionFailedException(AppCode.MESSAGE_SETED_FAILED, e.getCause());
        } catch (MessagingException e) {
            // Throw exception if it is failed during setting email
            throw new ActionFailedException(AppCode.MESSAGE_SETED_FAILED, e.getCause());
        }

    }

    @Override
    public void sendEmailWithHtmlBodyAndFilePath(String to, String cc, String bcc, String subject, String body,
            String filePath) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            // Config Subject, Text and Sender
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setTo(to);
            if (cc != null) {
                helper.setCc(cc);
            }
            if (bcc != null) {
                helper.setBcc(bcc);
            }
            // attach file
            FileSystemResource file = new FileSystemResource(new File(filePath));
            helper.addAttachment(file.getFilename(), file);
            // Send email
            javaMailSender.send(mimeMessage);
        } catch (MailSendException e) {
            // Throw exception if it is failed during sending email
            throw new ActionFailedException(AppCode.MESSAGE_SETED_FAILED, e.getCause());
        } catch (MessagingException e) {
            // Throw exception if it is failed during setting email
            throw new ActionFailedException(AppCode.MESSAGE_SETED_FAILED, e.getCause());
        }

    }

}
