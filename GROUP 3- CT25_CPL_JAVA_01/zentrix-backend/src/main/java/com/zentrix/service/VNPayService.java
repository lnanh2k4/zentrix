package com.zentrix.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Collections;
import java.util.TimeZone;

import org.springframework.stereotype.Service;

import com.zentrix.configuration.VNPayConfig;

import jakarta.servlet.http.HttpServletRequest;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
@Service
public class VNPayService {

    /**
     * Creates an order for VNPay payment gateway.
     * 
     * @param total      The total amount for the payment (in VND).
     * @param orderInfor A description of the order.
     * @param urlReturn  The URL to return to after the payment.
     * @param request    The HttpServletRequest to get the client's IP address.
     * @return A string URL to redirect the user to VNPay's payment page.
     */
    public String createOrder(long total, String orderInfor, String urlReturn, HttpServletRequest request) {
        // Set VNPay version and command for payment
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";

        // Generate a random transaction reference number
        String vnp_TxnRef = VNPayConfig.getRandomNumber(8);

        // Get the client's IP address from the request
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);

        // Set VNPay terminal code
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        // Define the order type
        String orderType = "order-type";

        // Initialize parameters map for VNPay request
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(total * 100)); // Convert total to VND (multiplying by 100)
        vnp_Params.put("vnp_CurrCode", "VND"); // Set currency to VND

        // Set transaction reference and order information
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfor);
        vnp_Params.put("vnp_OrderType", orderType);

        // Set locale to Vietnamese
        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

        // Set the return URL after transaction completion
        urlReturn += VNPayConfig.vnp_Returnurl;
        vnp_Params.put("vnp_ReturnUrl", urlReturn);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // Set creation and expiration times for the order (15 minutes)
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        // Expiration time: 15 minutes from creation
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sort parameters and build query string
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                // Build query string
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // Build secure hash using the secret key
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        // Return the full URL for VNPay's payment gateway
        return VNPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    /**
     * Processes the response after payment and checks the transaction status.
     * 
     * @param request The HttpServletRequest containing the response parameters.
     * @return 1 if the transaction is successful, 0 if failed, -1 for invalid
     *         signature.
     */
    public int orderReturn(HttpServletRequest request) {
        // Collect all response parameters from the request
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = URLEncoder.encode(params.nextElement(), StandardCharsets.US_ASCII);
            String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                fields.put(fieldName, fieldValue);
            }
        }

        // Get the secure hash sent from VNPay
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");

        // Remove secure hash and type fields from the parameters before hashing
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // Generate the hash from the parameters
        String signValue = VNPayConfig.hashAllFields(fields);

        // Compare the generated hash with the secure hash from VNPay
        if (signValue.equals(vnp_SecureHash)) {
            // Check the transaction status
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                return 1; // Transaction successful
            } else {
                return 0; // Transaction failed
            }
        } else {
            return -1; // Invalid signature (security issue)
        }
    }
}
