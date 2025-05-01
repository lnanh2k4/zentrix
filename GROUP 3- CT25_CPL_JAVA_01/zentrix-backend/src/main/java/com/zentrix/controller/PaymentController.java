package com.zentrix.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import com.mservice.shared.exception.MoMoException;
import com.zentrix.service.MomoService;
import com.zentrix.service.VNPayService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/api/v1/payment")
@Tag(name = "Payment Controller", description = "This class handles the payment process through VNPay integration.")

@AllArgsConstructor
public class PaymentController {

    VNPayService vnPayService;
    MomoService momoService;

    // Sử dụng Set để lưu các transactionId đã xử lý (trong bộ nhớ)
    private static final Set<String> processedTransactions = new HashSet<>();

    /**
     * Creates a payment URL for VNPay.
     */
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Create Payment URL", description = "Generates a payment URL for VNPay based on the total amount.")
    @PostMapping("/create")
    public ResponseEntity<String> createPayment(@RequestParam long totalAmount, HttpServletRequest request) {
        try {
            String returnUrl = "http://localhost:5173/PaymentSuccessPage";
            String paymentUrl = vnPayService.createOrder(totalAmount, "Thanh toán đơn hàng", returnUrl, request);
            return ResponseEntity.status(HttpStatus.OK).body(paymentUrl);
        } catch (Exception e) {
            System.err.println("Error while creating payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Handles the return from VNPay after a payment attempt.
     */
    @Operation(summary = "Handle Payment Return", description = "Handles the return response from VNPay after payment is completed.")
    @GetMapping("/return")
    public ResponseEntity<Map<String, Object>> handlePaymentReturn(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String transactionId = request.getParameter("vnp_TransactionNo");

            // Kiểm tra xem transactionId đã được xử lý chưa
            synchronized (processedTransactions) {
                if (processedTransactions.contains(transactionId)) {
                    response.put("status", "error");
                    response.put("message", "Transaction already processed.");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // Trả về 409 Conflict
                }
            }

            int paymentStatus = vnPayService.orderReturn(request);
            String amount = request.getParameter("vnp_Amount");
            String formattedAmount = String.valueOf(Long.parseLong(amount) / 100);

            if (paymentStatus == 1) {
                synchronized (processedTransactions) {
                    processedTransactions.add(transactionId);
                }

                response.put("status", "success");
                response.put("message", "Payment successful!");
                response.put("transactionId", transactionId);
                response.put("amount", formattedAmount);
            } else {
                response.put("status", "error");
                response.put("message", "Payment failed. Please try again.");
                response.put("transactionId", transactionId);
                response.put("amount", formattedAmount);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred while processing the payment.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Creates a payment URL for MoMo.
     */
    @PostMapping("/create-momo")
    @Operation(summary = "Create a MoMo payment URL", description = "Generates a payment URL for MoMo based on the total amount.")
    public ResponseEntity<String> createPaymentMomo(
            @RequestParam long totalAmount,
            @RequestParam(value = "orderInfo", defaultValue = "Thanh toán đơn hàng") String orderInfo,
            @RequestParam(value = "username", required = false) String username) throws MoMoException {
        String paymentUrl = momoService.createPayment(totalAmount, orderInfo, username);
        return ResponseEntity.status(HttpStatus.OK).body(paymentUrl);
    }

    /**
     * Handles the return from MoMo after a payment attempt.
     */
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Handle MoMo Payment Return", description = "Handles the return response from MoMo after payment is completed.")
    @GetMapping("/return-momo")
    public ResponseEntity<Map<String, Object>> handleMomoPaymentReturn(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String transactionId = request.getParameter("transId");

            synchronized (processedTransactions) {
                if (processedTransactions.contains(transactionId)) {
                    response.put("status", "error");
                    response.put("message", "Transaction already processed.");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // Trả về 409 Conflict
                }
            }

            int paymentStatus = momoService.verifyPayment(request);
            String amount = request.getParameter("amount");

            if (paymentStatus == 1) {
                synchronized (processedTransactions) {
                    processedTransactions.add(transactionId);
                }

                response.put("status", "success");
                response.put("message", "Payment successful!");
                response.put("transactionId", transactionId);
                response.put("amount", amount);
            } else {
                response.put("status", "error");
                response.put("message", "Payment failed. Please try again.");
                response.put("transactionId", transactionId);
                response.put("amount", amount);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred while processing the payment.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}