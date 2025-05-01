package com.zentrix.controller;

import com.zentrix.service.OrderService;
import com.zentrix.service.UserService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.zentrix.model.entity.Order;
import com.zentrix.model.entity.OrderDetail;
import com.zentrix.model.entity.User;
import com.zentrix.model.request.OrderDetailRequest;
import com.zentrix.model.request.OrderRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.EmailService;
import com.zentrix.service.OrderDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/*
* @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
* @date February 17, 2025
*/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Controller", description = "This class supports OrderAPI")
public class OrderController {

        OrderService orderService;

        OrderDetailService orderDetailService;
        EmailService emailService; // Thêm dependency EmailService
        UserService userService; // Thêm dependency UserService
        private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

        private String getBase64Image(String imagePath) throws Exception {
                byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        }

        /**
         * This API allows to create a new order
         *
         * @param orderRequest Order request containing order details
         * @return Created order information
         */

        @PostMapping("/addOrder")
        @PreAuthorize("hasRole('SELLER STAFF') or hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Create a new order", description = "This API allows to create a new order")

        public ResponseEntity<ResponseObject<Order>> createOrder(@RequestBody OrderRequest orderRequest
        // @RequestHeader("Authorization") String jwt
        ) {

                Order order = orderService.addOrder(orderRequest);

                ResponseObject<Order> response = new ResponseObject.Builder<Order>()
                                .content(
                                                order)
                                .message("Create user successfully")
                                .code(HttpStatus.CREATED.value())
                                .success(true)
                                .build();

                return ResponseEntity.status(HttpStatus.CREATED.value()).body(response);
        }

        /**
         * Generates a PDF invoice for the specified order and sends it via email.
         * 
         * @param orderId The ID of the order for which the invoice will be generated
         *                and sent.
         * @return A ResponseEntity containing a ResponseObject with the result of the
         *         operation.
         */
        @PostMapping("/{orderId}/generateInvoiceAndSendEmail")
        @PreAuthorize("hasRole('SELLER STAFF') or hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Generate invoice and send email", description = "This API generates a PDF invoice for the order and sends it via email")
        public ResponseEntity<ResponseObject<String>> generateInvoiceAndSendEmail(@PathVariable Long orderId) {
                try {
                        // Retrieve the order by its ID
                        Order order = orderService.findOrderById(orderId);
                        if (order == null) {
                                throw new RuntimeException("Order not found with ID: " + orderId);
                        }

                        // Get the user ID from the order
                        Long userId = order.getUserId().getUserId();
                        if (userId == null) {
                                throw new RuntimeException("User ID not found for order ID: " + orderId);
                        }

                        // Retrieve the user by their ID
                        User user = userService.findUserByUserId(userId);
                        if (user == null) {
                                throw new RuntimeException("User not found for userId: " + userId);
                        }

                        // Extract user details
                        String userEmail = user.getEmail();
                        String fullName = user.getFirstName() + " " + user.getLastName();
                        String phone = user.getPhone();

                        // Generate the PDF invoice file
                        String pdfFilePath = generateOrderPDF(order, fullName, phone);

                        if (userEmail != null && !userEmail.isEmpty()) {
                                try {
                                        // Log the email sending process
                                        logger.info("Sending order invoice email with PDF to: {}", userEmail);

                                        // Use the new logo URL
                                        String logoUrl = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiFiXwQ_maatzhJ8oajcgfHJc1mpwv1UDv_pWLFT53HHtUaRZkvOQcuhofX5tamuITa9dxqKQI5wJQ3e3wNqnn1K7sxwdMjGJHY8E_BLsizh8quZdyWs9SX0elWyXJN3Aps-bxHI0wvGvahJapqjJaBJBPXvg5fHqewoiz0RU7ApdukKvi9kbdbSxzPtbqI/s1628/logo%20new%20(1).png";

                                        // Create the email content in HTML format with a polished design
                                        StringBuilder emailContent = new StringBuilder();
                                        emailContent.append(
                                                        "<!DOCTYPE html><html lang='en'><body style='margin: 0; padding: 0; background-color: #f4f4f4; font-family: Arial, sans-serif;'>");
                                        emailContent.append(
                                                        "<div style='max-width: 650px; margin: 30px auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 6px 18px rgba(0,0,0,0.1); overflow: hidden;'>");

                                        // Header with logo and title
                                        emailContent.append(
                                                        "<div style='background-color: #1a5276; padding: 20px; text-align: center; color: #ffffff;'>");
                                        emailContent.append(
                                                        "<div style='display: flex; align-items: center; justify-content: center;'>");
                                        emailContent.append(
                                                        "<img src='").append(logoUrl)
                                                        .append("' style='height: 40px; margin-right: 15px; display: inline-block; vertical-align: middle;' alt='Zentrix Logo'/>");
                                        emailContent.append(
                                                        "<div style='display: inline-block; vertical-align: middle; text-align: left;'>");
                                        emailContent.append(
                                                        "<h1 style='margin: 0; font-size: 28px; font-weight: 700; color: #ffffff;'>Zentrix Store</h1>");
                                        emailContent.append(
                                                        "<p style='margin: 5px 0 0; font-size: 14px; opacity: 0.9;'>Your Trusted Tech Partner</p>");
                                        emailContent.append("</div>");
                                        emailContent.append("</div>");
                                        emailContent.append("</div>");

                                        // Invoice notification section
                                        emailContent.append(
                                                        "<div style='background-color: #e8f4f8; padding: 15px; text-align: center; border-bottom: 1px solid #e0e0e0;'>");
                                        emailContent.append(
                                                        "<h2 style='color: #1a5276; font-size: 22px; margin: 0; font-weight: 600;'>Order Invoice</h2>");
                                        emailContent.append(
                                                        "<p style='color: #555555; font-size: 13px; margin: 5px 0 0;'>Invoice #"
                                                                        + order.getOrderId() + " | "
                                                                        + new SimpleDateFormat("dd/MM/yyyy")
                                                                                        .format(new Date())
                                                                        + "</p>");
                                        emailContent.append("</div>");

                                        // Customer information section
                                        emailContent.append("<div style='padding: 25px 30px;'>");
                                        emailContent.append(
                                                        "<h3 style='color: #333333; font-size: 20px; margin: 0 0 15px; font-weight: 600; border-bottom: 2px solid #1a5276; padding-bottom: 5px;'>Customer Information</h3>");
                                        emailContent.append(
                                                        "<p style='margin: 8px 0; font-size: 15px; color: #555555; line-height: 1.6;'><strong style='color: #333333; display: inline-block; width: 120px;'>Name:</strong> ")
                                                        .append(fullName).append("</p>");
                                        emailContent.append(
                                                        "<p style='margin: 8px 0; font-size: 15px; color: #555555; line-height: 1.6;'><strong style='color: #333333; display: inline-block; width: 120px;'>Phone:</strong> ")
                                                        .append(phone).append("</p>");
                                        emailContent.append(
                                                        "<p style='margin: 8px 0; font-size: 15px; color: #555555; line-height: 1.6;'><strong style='color: #333333; display: inline-block; width: 120px;'>Email:</strong> <a href='mailto:")
                                                        .append(userEmail)
                                                        .append("' style='color: #1a5276; text-decoration: none;'>")
                                                        .append(userEmail).append("</a></p>");
                                        emailContent.append("</div>");

                                        // Order details section
                                        emailContent.append(
                                                        "<div style='padding: 25px 30px; border-top: 1px solid #e0e0e0;'>");
                                        emailContent.append(
                                                        "<h3 style='color: #333333; font-size: 20px; margin: 0 0 15px; font-weight: 600; border-bottom: 2px solid #1a5276; padding-bottom: 5px;'>Order Details</h3>");
                                        emailContent.append(
                                                        "<table style='border-collapse: collapse; width: 100%; font-size: 14px; color: #555555;'>");

                                        // Table header
                                        emailContent.append("<thead>");
                                        emailContent.append("<tr style='background-color: #e8f4f8; color: #333333;'>");
                                        emailContent.append(
                                                        "<th style='padding: 12px 15px; text-align: left; font-weight: 600; border-bottom: 1px solid #e0e0e0; width: 40%;'>Field</th>");
                                        emailContent.append(
                                                        "<th style='padding: 12px 15px; text-align: left; font-weight: 600; border-bottom: 1px solid #e0e0e0; width: 60%;'>Details</th>");
                                        emailContent.append("</tr>");
                                        emailContent.append("</thead>");

                                        // Table body
                                        emailContent.append("<tbody>");
                                        emailContent.append("<tr style='background-color: #fafafa;'>");
                                        emailContent.append(
                                                        "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Order ID</td>");
                                        emailContent.append(
                                                        "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; color: #1a5276; font-weight: 500;'>")
                                                        .append(order.getOrderId()).append("</td>");
                                        emailContent.append("</tr>");
                                        emailContent.append("<tr>");
                                        emailContent.append(
                                                        "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Order Date</td>");
                                        emailContent.append(
                                                        "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; font-weight: 500;'>")
                                                        .append(new SimpleDateFormat("dd/MM/yyyy")
                                                                        .format(new java.util.Date()))
                                                        .append("</td>");
                                        emailContent.append("</tr>");
                                        emailContent.append("<tr style='background-color: #fafafa;'>");
                                        emailContent.append(
                                                        "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Payment Method</td>");
                                        emailContent.append(
                                                        "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>")
                                                        .append(order.getPaymentMethod() != null
                                                                        ? order.getPaymentMethod()
                                                                        : "N/A")
                                                        .append("</td>");
                                        emailContent.append("</tr>");
                                        emailContent.append("</tbody>");
                                        emailContent.append("</table>");
                                        emailContent.append("</div>");

                                        // Attachment information
                                        emailContent.append(
                                                        "<div style='padding: 20px 30px; border-top: 1px solid #e0e0e0; background-color: #f9f9f9;'>");
                                        emailContent.append(
                                                        "<p style='margin: 0; font-size: 15px; color: #555555; line-height: 1.6; text-align: center;'><strong style='color: #1a5276;'>Attachment:</strong> Your order invoice is attached in PDF format.</p>");
                                        emailContent.append("</div>");

                                        // Thank you message and footer
                                        emailContent.append(
                                                        "<div style='padding: 25px 30px; text-align: center; background-color: #1a5276; color: #ffffff; border-radius: 0 0 12px 12px;'>");
                                        emailContent.append(
                                                        "<p style='margin: 0 0 10px; font-size: 15px; line-height: 1.6;'>Thank you for choosing Zentrix Store!</p>");
                                        emailContent.append(
                                                        "<p style='margin: 0; font-size: 14px; font-weight: 600;'>Best regards,<br>The Zentrix Team</p>");
                                        emailContent.append(
                                                        "<p style='margin: 15px 0 0; font-size: 13px; opacity: 0.9;'>Hotline: 0393.510.720 | Email: <a href='mailto:support@zentrix.com' style='color: #ffffff; text-decoration: underline;'>support@zentrix.com</a></p>");
                                        emailContent.append("</div>");

                                        // Close the container
                                        emailContent.append("</div>");
                                        emailContent.append("</body></html>");

                                        // Send the email with HTML body and the PDF attachment
                                        emailService.sendEmailWithHtmlBodyAndFilePath(userEmail, null, null,
                                                        "Zentrix Store - Invoice #" + order.getOrderId()
                                                                        + " | Payment Confirmed "
                                                                        + new SimpleDateFormat("dd/MM/yyyy")
                                                                                        .format(new Date()),
                                                        emailContent.toString(), pdfFilePath);
                                        logger.info("Order invoice email with PDF sent successfully to: {}", userEmail);
                                } catch (Exception e) {
                                        // Log the error if email sending fails
                                        logger.error("Failed to send order invoice email with PDF to {}: {}", userEmail,
                                                        e.getMessage());
                                        throw new RuntimeException("Failed to send email: " + e.getMessage());
                                } finally {
                                        // Clean up the temporary PDF file
                                        File pdfFile = new File(pdfFilePath);
                                        if (pdfFile.exists()) {
                                                pdfFile.delete();
                                                logger.info("Temporary PDF file deleted: {}", pdfFilePath);
                                        }
                                }
                        } else {
                                // Log a warning if the user email is missing
                                logger.warn("User email is null or empty for userId: {}", userId);
                                throw new RuntimeException("User email is null or empty for userId: " + userId);
                        }

                        // Build and return a successful response
                        ResponseObject<String> response = new ResponseObject.Builder<String>()
                                        .content("Invoice generated and sent successfully")
                                        .message("Invoice generated and sent successfully")
                                        .code(HttpStatus.OK.value())
                                        .success(true)
                                        .build();

                        return ResponseEntity.status(HttpStatus.OK).body(response);

                } catch (Exception e) {
                        // Build and return an error response if any exception occurs
                        ResponseObject<String> errorResponse = new ResponseObject.Builder<String>()
                                        .message("Failed to generate invoice and send email: " + e.getMessage())
                                        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .success(false)
                                        .build();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }
        }

        private String generateOrderPDF(Order order, String fullName, String phone) {
                String fileName = "order_" + order.getOrderId() + ".pdf";
                String filePath = System.getProperty("java.io.tmpdir") + File.separator + fileName;

                try {
                        PdfWriter writer = new PdfWriter(filePath);
                        PdfDocument pdf = new PdfDocument(writer);
                        Document document = new Document(pdf);
                        document.setFontSize(11);

                        // Đường dẫn đến logo trong thư mục uploads
                        String logoPath = "uploads/logoFULL.png";

                        // Tạo hình ảnh logo
                        Image logo = new Image(ImageDataFactory.create(logoPath))
                                        .setHeight(50) // Tăng chiều cao logo để nổi bật
                                        .setAutoScaleWidth(true)
                                        .setTextAlignment(TextAlignment.LEFT);

                        // Tạo tiêu đề chính với logo và text
                        Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 20, 80 }))
                                        .useAllAvailableWidth()
                                        .setMarginBottom(15);

                        // Ô chứa logo
                        headerTable.addCell(new Cell()
                                        .add(logo)
                                        .setBorder(Border.NO_BORDER)
                                        .setTextAlignment(TextAlignment.LEFT));

                        // Ô chứa text "Zentrix Store"
                        headerTable.addCell(new Cell()
                                        .add(new Paragraph("Zentrix Store")
                                                        .setFontSize(24) // Tăng kích thước chữ
                                                        .setBold()
                                                        .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 82,
                                                                        118))) // Màu xanh đậm #1a5276
                                        .setBorder(Border.NO_BORDER)
                                        .setTextAlignment(TextAlignment.LEFT)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE));

                        document.add(headerTable);

                        // Slogan
                        document.add(new Paragraph("Your Trusted Tech Partner")
                                        .setFontSize(12)
                                        .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 82, 118))
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setMarginBottom(10));

                        // Dòng phân cách
                        document.add(new Paragraph("--------------------------------------------------")
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setFontColor(ColorConstants.LIGHT_GRAY));

                        // Tiêu đề hóa đơn
                        document.add(new Paragraph("ORDER INVOICE - #" + order.getOrderId())
                                        .setFontSize(18)
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setFontColor(ColorConstants.BLACK)
                                        .setMarginTop(10)
                                        .setMarginBottom(10));

                        // Thông tin công ty
                        document.add(new Paragraph("Zentrix Information Technology Solutions")
                                        .setFontSize(14)
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 82, 118)));
                        document.add(new Paragraph("Trading Services Company Limited")
                                        .setFontSize(14)
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 82, 118)));
                        document.add(new Paragraph("Hotline: 0393.510.720")
                                        .setFontSize(10)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setFontColor(ColorConstants.GRAY)
                                        .setMarginBottom(15));

                        // Dòng phân cách
                        document.add(new Paragraph("--------------------------------------------------")
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setFontColor(ColorConstants.LIGHT_GRAY));

                        // Thông tin khách hàng
                        document.add(new Paragraph("Customer Information")
                                        .setFontSize(16)
                                        .setBold()
                                        .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 82, 118))
                                        .setMarginTop(20)
                                        .setMarginBottom(10));
                        document.add(new Paragraph("Customer: " + (fullName != null ? fullName : "N/A"))
                                        .setFontSize(11)
                                        .setMarginLeft(20));
                        document.add(new Paragraph("Phone: " + (phone != null ? phone : "N/A"))
                                        .setFontSize(11)
                                        .setMarginLeft(20));
                        document.add(new Paragraph("Order Date: "
                                        + new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()))
                                        .setFontSize(11)
                                        .setMarginLeft(20));
                        document.add(new Paragraph("Payment Method: "
                                        + (order.getPaymentMethod() != null ? order.getPaymentMethod() : "N/A"))
                                        .setFontSize(11)
                                        .setMarginLeft(20)
                                        .setMarginBottom(15));

                        // Bảng chi tiết đơn hàng
                        document.add(new Paragraph("Order Details")
                                        .setFontSize(16)
                                        .setBold()
                                        .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 82, 118))
                                        .setMarginTop(20)
                                        .setMarginBottom(10));
                        List<OrderDetail> orderDetails = orderDetailService.findByOrderID(order, 0, Integer.MAX_VALUE)
                                        .getData();
                        float[] columnWidths = { 4, 2, 3, 3 }; // Tỷ lệ cột
                        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                                        .useAllAvailableWidth()
                                        .setMarginTop(10);

                        // Tiêu đề bảng
                        table.addHeaderCell(new Cell()
                                        .add(new Paragraph("Product Type").setFontSize(11).setBold())
                                        .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(232, 244, 248)) // Màu
                                                                                                                     // nền
                                                                                                                     // #e8f4f8
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setBorder(new SolidBorder(ColorConstants.GRAY, 1)));
                        table.addHeaderCell(new Cell()
                                        .add(new Paragraph("Quantity").setFontSize(11).setBold())
                                        .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(232, 244, 248))
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setBorder(new SolidBorder(ColorConstants.GRAY, 1)));
                        table.addHeaderCell(new Cell()
                                        .add(new Paragraph("Unit Price").setFontSize(11).setBold())
                                        .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(232, 244, 248))
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setBorder(new SolidBorder(ColorConstants.GRAY, 1)));
                        table.addHeaderCell(new Cell()
                                        .add(new Paragraph("Total").setFontSize(11).setBold())
                                        .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(232, 244, 248))
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setBorder(new SolidBorder(ColorConstants.GRAY, 1)));

                        // Nội dung bảng
                        double totalPrice = 0;
                        boolean alternateRow = false;
                        for (OrderDetail detail : orderDetails) {
                                table.addCell(new Cell()
                                                .add(new Paragraph(detail.getProdTypeBranchId() != null
                                                                ? detail.getProdTypeBranchId().getProdTypeId()
                                                                                .getProdTypeName()
                                                                : "N/A")
                                                                .setFontSize(10))
                                                .setBackgroundColor(alternateRow
                                                                ? new com.itextpdf.kernel.colors.DeviceRgb(250, 250,
                                                                                250)
                                                                : ColorConstants.WHITE) // Xen kẽ màu nền
                                                .setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f)));
                                table.addCell(new Cell()
                                                .add(new Paragraph(String.valueOf(detail.getQuantity()))
                                                                .setFontSize(10))
                                                .setBackgroundColor(alternateRow
                                                                ? new com.itextpdf.kernel.colors.DeviceRgb(250, 250,
                                                                                250)
                                                                : ColorConstants.WHITE)
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f)));
                                double unitPrice = detail.getUnitPrice();
                                table.addCell(new Cell()
                                                .add(new Paragraph(String.format("%,.0f VND", unitPrice))
                                                                .setFontSize(10))
                                                .setBackgroundColor(alternateRow
                                                                ? new com.itextpdf.kernel.colors.DeviceRgb(250, 250,
                                                                                250)
                                                                : ColorConstants.WHITE)
                                                .setTextAlignment(TextAlignment.RIGHT)
                                                .setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f)));
                                double total = unitPrice * detail.getQuantity();
                                table.addCell(new Cell()
                                                .add(new Paragraph(String.format("%,.0f VND", total)).setFontSize(10))
                                                .setBackgroundColor(alternateRow
                                                                ? new com.itextpdf.kernel.colors.DeviceRgb(250, 250,
                                                                                250)
                                                                : ColorConstants.WHITE)
                                                .setTextAlignment(TextAlignment.RIGHT)
                                                .setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f)));
                                totalPrice += total;
                                alternateRow = !alternateRow;
                        }

                        // Dòng tổng
                        table.addCell(new Cell(1, 2)
                                        .add(new Paragraph(""))
                                        .setBorder(Border.NO_BORDER));
                        table.addCell(new Cell()
                                        .add(new Paragraph("TOTAL:").setFontSize(11).setBold())
                                        .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(232, 244, 248))
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f)));
                        table.addCell(new Cell()
                                        .add(new Paragraph(String.format("%,.0f VND", totalPrice)).setFontSize(11)
                                                        .setBold())
                                        .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(232, 244, 248))
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f)));

                        document.add(table);

                        // Ghi chú
                        document.add(new Paragraph("Note:")
                                        .setFontSize(10)
                                        .setBold()
                                        .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 82, 118))
                                        .setMarginTop(20));
                        document.add(new Paragraph(
                                        "Please check your order details and contact us if there are any issues.")
                                        .setFontSize(10)
                                        .setMarginLeft(20)
                                        .setMarginBottom(20));

                        // Footer
                        document.add(new Paragraph("--------------------------------------------------")
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setFontColor(ColorConstants.LIGHT_GRAY));
                        document.add(new Paragraph("ZENTRIX.STORE")
                                        .setFontSize(12)
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(26, 82, 118)));
                        document.add(new Paragraph("HOTLINE: 0393.510.720 | Email: support@zentrix.com")
                                        .setFontSize(10)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setFontColor(ColorConstants.GRAY));

                        document.close();
                        logger.info("PDF generated successfully: {}", filePath);
                } catch (Exception e) {
                        logger.error("Failed to generate PDF: {}", e.getMessage(), e);
                        throw new RuntimeException("Failed to generate order PDF", e);
                }

                return filePath;
        }

        /**
         * This API allows to get a list of all orders
         *
         * @return List of all orders
         */
        @PreAuthorize("hasAnyRole('SELLER STAFF','ADMIN')   ")
        @GetMapping()
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get all orders", description = "This API allows to get a list of all orders")

        public ResponseEntity<ResponseObject<List<Order>>> getAllOrders(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size
        // @RequestHeader("Authorization") String jwt
        ) {

                PaginationWrapper<List<Order>> orders = orderService.findAllOrders(page, size);
                ResponseObject<List<Order>> response = new ResponseObject.Builder<List<Order>>()
                                .unwrapPaginationWrapper(
                                                orders)
                                .message("Get orders list successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * This API allows to get an order by order ID
         *
         * @param orderId ID of the order to fetch
         * @return Order details
         */
        @PreAuthorize("hasRole('SELLER STAFF')  or hasRole('ADMIN')")
        @GetMapping("/{id}")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get order by ID", description = "This API allows togetan orderby order ID")

        public ResponseEntity<ResponseObject<Order>> getOrderById(@PathVariable Long orderId

        // @RequestHeader("Authorization") String jwt
        ) {
                Order order = orderService.findOrderById(orderId);
                ResponseObject<Order> response = new ResponseObject.Builder<Order>()
                                .content(order)
                                .message("Get user by Id successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.status(HttpStatus.OK.value()).body(response);

        }

        @PreAuthorize("hasAnyRole('SELLER STAFF') or hasAnyRole('SHIPPER') or hasAnyRole('ADMIN')")
        @PutMapping("/update")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Update order", description = "This API allows to update an order")
        public ResponseEntity<ResponseObject<Order>> updateOrder(
                        @RequestParam Long orderId,
                        @RequestBody OrderRequest orderRequest) {
                Order existingOrder = orderService.findOrderById(orderId);
                if (orderRequest.getStatus() == 4 && existingOrder.getStatus() != 4) {
                        orderDetailService.restoreQuantitiesForOrder(orderId);
                }
                Order updatedOrder = orderService.updateOrder(orderId, orderRequest);
                ResponseObject<Order> response = new ResponseObject.Builder<Order>()
                                .content(updatedOrder)
                                .message("Update order successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        /**
         * This API allows to search for orders based on a keyword
         *
         * @param keyword Search term
         * @return List of orders matching the search term
         */
        @PreAuthorize("hasAnyRole('SELLER STAFF','ADMIN')")
        @GetMapping("/search")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Search orders", description = "This API allows tosearchfor orders basedon a keyword")

        public ResponseEntity<ResponseObject<List<Order>>> searchOrders(@RequestParam String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size
        // @RequestHeader("Authorization") String jwt
        ) {
                PaginationWrapper<List<Order>> orders = orderService.searchOrder(keyword, page, size);
                ResponseObject<List<Order>> response = new ResponseObject.Builder<List<Order>>()
                                .unwrapPaginationWrapper(orders)
                                .message("Get customers list successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * This API allows to get all order details for a specific order
         *
         * @param orderId ID of the order
         * @return List of order details
         */
        @PreAuthorize("hasAnyRole('SELLER STAFF') or hasAnyRole('ADMIN')")
        @GetMapping("/{orderId}/details")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get order details", description = "This API allowstoget alldetails ofa specific order")

        public ResponseEntity<ResponseObject<List<OrderDetail>>> getOrderDetails(@PathVariable Long orderId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size
        // @RequestHeader("Authorization") String jwt
        ) {
                Order order = orderService.findOrderById(orderId);
                PaginationWrapper<List<OrderDetail>> orderDetails = orderDetailService.findByOrderID(order, page, size);
                ResponseObject<List<OrderDetail>> response = new ResponseObject.Builder<List<OrderDetail>>()
                                .unwrapPaginationWrapper(
                                                orderDetails)
                                .message("Get customers list successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        @PreAuthorize("hasAnyRole('CUSTOMER') or hasAnyRole('ADMIN')")
        @PostMapping("/addOrderDetail")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Create a new order detail", description = "This API allows tocreate a new order")

        public ResponseEntity<ResponseObject<OrderDetail>> createOrderDetail(
                        @RequestBody OrderDetailRequest orderDetailRequest
        // @RequestHeader("Authorization") String jwt
        ) {

                // Create the new order
                OrderDetail orderdDetail = orderDetailService.saveOrderDetail(orderDetailRequest);

                ResponseObject<OrderDetail> response = new ResponseObject.Builder<OrderDetail>()
                                .content(
                                                orderdDetail)
                                .message("Create user successfully")
                                .code(HttpStatus.CREATED.value())
                                .success(true)
                                .build();

                return ResponseEntity.status(HttpStatus.CREATED.value()).body(response);
        }

        /**
         * This API allows to get all orders for a specific user
         *
         * @param userId ID of the user
         * @param page   Page number for pagination (default: 0)
         * @param size   Number of items per page (default: 10)
         * @return List of orders for the user
         */
        @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
        @GetMapping("/user/{userId}")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get orders by user ID", description = "This API allows to get all orders for a specific user")
        public ResponseEntity<ResponseObject<List<Order>>> getOrdersByUser(
                        @PathVariable Long userId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                // Create User with userid
                User user = new User();
                user.setUserId(userId);

                PaginationWrapper<List<Order>> orders = orderService.findOrdersByUser(user, page, size);

                ResponseObject<List<Order>> response = new ResponseObject.Builder<List<Order>>()
                                .unwrapPaginationWrapper(orders)
                                .message("Get user's orders successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();

                return ResponseEntity.ok(response);
        }
}
