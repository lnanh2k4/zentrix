package com.zentrix.service;

import com.mservice.shared.exception.MoMoException;
import com.zentrix.configuration.CustomEnvironment;
import com.zentrix.model.response.PaymentResponse;
import com.zentrix.model.utils.LogUtils;
import com.zentrix.model.utils.RequestType;
import com.zentrix.processor.CreateOrderMomo;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.HmacUtils;
import java.util.HashMap;
import java.util.Map;

@Service
public class MomoServiceImpl implements MomoService {

    private final String accessKey = "mTCKt9W3eU1m39TW"; // Thay bằng accessKey của bạn
    private final String secretKey = "SetA5RDnLHvt51AULf51DyauxUo3kDU6"; // Thay bằng secretKey của bạn
    private final String partnerCode = "MOMOLRJZ20181206"; // Thay bằng partnerCode của bạn

    @Override
    public String createPayment(long totalAmount, String orderInfo, String username) throws MoMoException {
        LogUtils.init(); // Initialize logging

        String requestId = String.valueOf(System.currentTimeMillis());
        String orderId = String.valueOf(System.currentTimeMillis());

        String returnUrl = "http://localhost:5173/PaymentSuccessPage";
        String notifyUrl = "https://google.com.vn";

        CustomEnvironment environment = CustomEnvironment.selectEnv("dev");

        PaymentResponse paymentResponse = CreateOrderMomo.process(environment,
                orderId,
                requestId,
                String.valueOf(totalAmount),
                orderInfo,
                returnUrl,
                notifyUrl,
                "",
                RequestType.PAY_WITH_ATM,
                Boolean.TRUE);

        if (paymentResponse != null && paymentResponse.getResultCode() == 0) {
            return paymentResponse.getPayUrl();
        } else {
            throw new MoMoException("Failed to generate MoMo payment URL: " +
                    (paymentResponse != null ? paymentResponse.getMessage() : "Unknown error"));
        }
    }

    @Override
    public int verifyPayment(HttpServletRequest request) throws MoMoException {
        String transId = request.getParameter("transId");
        String orderId = request.getParameter("orderId");
        String amount = request.getParameter("amount");

        if (transId == null || orderId == null || amount == null) {
            return 0; // Thiếu thông tin, trả về thất bại
        }

        try {
            // Tạo dữ liệu yêu cầu cho API xác minh của MoMo
            Map<String, String> requestData = new HashMap<>();
            requestData.put("partnerCode", partnerCode);
            requestData.put("requestId", String.valueOf(System.currentTimeMillis()));
            requestData.put("orderId", orderId);
            requestData.put("lang", "vi");

            // Tạo chữ ký (signature)
            String rawData = "accessKey=" + accessKey +
                    "&orderId=" + orderId +
                    "&partnerCode=" + partnerCode +
                    "&requestId=" + requestData.get("requestId");
            String signature = HmacUtils.hmacSha256Hex(secretKey, rawData); // Sử dụng thư viện HmacUtils hoặc tự
                                                                            // implement
            requestData.put("signature", signature);

            // Gửi yêu cầu đến API MoMo
            String endpoint = "https://test-payment.momo.vn/v2/gateway/api/query"; // URL API test, thay bằng URL
                                                                                   // production nếu cần
            String jsonRequest = new ObjectMapper().writeValueAsString(requestData);

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(endpoint);
            httpPost.setEntity(new StringEntity(jsonRequest, "UTF-8"));
            httpPost.setHeader("Content-Type", "application/json");

            CloseableHttpResponse response = client.execute(httpPost);
            String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");

            // Phân tích phản hồi từ MoMo
            Map<String, Object> responseMap = new ObjectMapper().readValue(jsonResponse, HashMap.class);
            int resultCode = (int) responseMap.get("resultCode");

            // Kiểm tra mã kết quả (0 là thành công)
            if (resultCode == 0) {
                // Có thể kiểm tra thêm các thông tin như amount, transId nếu cần
                return 1; // Thanh toán thành công
            } else {
                return 0; // Thanh toán thất bại
            }

        } catch (Exception e) {
            throw new MoMoException("Error verifying MoMo payment: " + e.getMessage());
        }
    }
}