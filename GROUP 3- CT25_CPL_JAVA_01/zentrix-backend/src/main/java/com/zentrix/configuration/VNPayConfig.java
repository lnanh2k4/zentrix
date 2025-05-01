package com.zentrix.configuration;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
public class VNPayConfig {

    // VNPay configuration details
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"; // VNPay payment URL
    public static String vnp_Returnurl = ""; // Return URL after payment
    public static String vnp_TmnCode = "4FEYIVW6"; // Merchant ID (vnp_TmnCode)
    public static String vnp_HashSecret = "9WRHA3E2PSWNDT7I20U0CUURYYYBCLS8"; // Secret key for hashing
    public static String vnp_apiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction"; // VNPay API URL

    /**
     * Generates an MD5 hash of the given message.
     * 
     * @param message The message to be hashed.
     * @return The MD5 hash of the message.
     */
    public static String md5(String message) {
        String digest = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            // Perform the hash computation
            byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            // Convert byte array into hexadecimal string
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString(); // Return the hashed message
        } catch (NoSuchAlgorithmException ex) {
            digest = ""; // Return empty string if the hashing algorithm is not found
        }
        return digest;
    }

    /**
     * Creates a sorted query string from the provided map of fields.
     * Then, it generates a HMAC SHA512 hash of the query string using the secret
     * key.
     *
     * @param fields A map of fields and their values to be used in the hash
     *               generation.
     * @return The HMAC SHA512 hash of the sorted query string.
     */
    public static String hashAllFields(Map fields) {
        List fieldNames = new ArrayList(fields.keySet()); // Get list of field names
        Collections.sort(fieldNames); // Sort the field names in ascending order
        StringBuilder sb = new StringBuilder();
        Iterator itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) fields.get(fieldName);
            // Append field name and value to the query string if the value is not empty
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
            }
            // Append '&' between fields if there's another field
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        // Hash the query string using HMAC SHA512 and return the result
        return hmacSHA512(vnp_HashSecret, sb.toString());
    }

    /**
     * Generates an HMAC SHA512 hash using the provided key and data.
     * 
     * @param key  The secret key used for hashing.
     * @param data The data to be hashed.
     * @return The HMAC SHA512 hash of the data.
     */
    public static String hmacSHA512(final String key, final String data) {
        try {
            // Check for null inputs
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            // Initialize HMAC SHA512
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(); // Convert the key into bytes
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey); // Initialize the HMAC with the secret key
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8); // Convert data into bytes
            byte[] result = hmac512.doFinal(dataBytes); // Perform the HMAC computation
            StringBuilder sb = new StringBuilder(2 * result.length);
            // Convert byte array into hexadecimal string
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString(); // Return the HMAC SHA512 hash as a string
        } catch (Exception ex) {
            return ""; // Return empty string if there's an error during hashing
        }
    }

    /**
     * Retrieves the IP address of the client making the request.
     * 
     * @param request The HTTP request object.
     * @return The client's IP address.
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            // Get the X-Forwarded-For header to obtain the client's original IP address
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                // Fallback to the local address if X-Forwarded-For header is not present
                ipAdress = request.getLocalAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage(); // Return an error message if IP address retrieval fails
        }
        return ipAdress;
    }

    /**
     * Generates a random number of the specified length.
     * 
     * @param len The length of the random number.
     * @return A random number represented as a string.
     */
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789"; // Characters to be used for generating the random number
        StringBuilder sb = new StringBuilder(len);
        // Generate a random string of the specified length
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length()))); // Append random character to the string
        }
        return sb.toString(); // Return the generated random number as a string
    }
}
