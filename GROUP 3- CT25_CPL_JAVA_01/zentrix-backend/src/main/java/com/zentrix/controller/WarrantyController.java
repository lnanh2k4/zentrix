package com.zentrix.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.zentrix.model.entity.User;
import com.zentrix.model.entity.Warranty;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.request.WarrantyRequest;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.EmailService;
import com.zentrix.service.UserService;
import com.zentrix.service.WarrantyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Controller for handling Warranty-related operations.
 *
 * @author Dang Cong Khanh - CE180117 - CT25_CPL_JAVA_01
 * @date February 12, 2025
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/api/v1/warranties")
@Tag(name = "Warranty Controller", description = "APIs for managing warranties.")
public class WarrantyController {

        WarrantyService warrantyService;
        EmailService emailService;
        UserService userService;
        private static final Logger logger = LoggerFactory.getLogger(WarrantyController.class);

        /**
         * This method allows to create a new warranty with authorization for SELLER
         * STAFF or ADMIN roles
         * 
         * @param request     request body containing warranty details
         * @param createdById ID of the staff creating the warranty (optional)
         * @return ResponseEntity containing the created warranty and response metadata
         */
        @PreAuthorize("hasAnyRole('SELLER STAFF', 'ADMIN')")
        @Operation(summary = "Create a new warranty", description = "Saves a new warranty record to the database and sends a PDF receipt via email.", security = @SecurityRequirement(name = "Authorization"))
        @PostMapping
        public ResponseEntity<ResponseObject<Warranty>> createWarranty(
                        @RequestBody WarrantyRequest request,
                        @Parameter(description = "ID of the staff creating the warranty", required = false) @RequestParam(required = false) Long createdById) {
                Warranty savedWarranty = warrantyService.addWarranty(request, createdById);
                ResponseObject<Warranty> response = new ResponseObject.Builder<Warranty>()
                                .content(savedWarranty)
                                .message("Warranty created successfully")
                                .code(HttpStatus.CREATED.value())
                                .success(true)
                                .build();

                String userEmail = userService.findUserByUserId(request.getUserId()).getEmail();
                String fullName = userService.findUserByUserId(request.getUserId()).getFirstName() + " " +
                                userService.findUserByUserId(request.getUserId()).getLastName();
                String phone = userService.findUserByUserId(request.getUserId()).getPhone();

                String pdfFilePath = generateWarrantyPDF(savedWarranty, fullName, phone, createdById);

                if (userEmail != null && !userEmail.isEmpty()) {
                        try {
                                logger.info("Sending warranty receipt email with PDF to: {}", userEmail);

                                // Nội dung HTML chi tiết
                                StringBuilder emailContent = new StringBuilder();
                                emailContent.append(
                                                "<!DOCTYPE html><html lang='en'><body style='margin: 0; padding: 0; background-color: #f4f4f4; font-family: Arial, sans-serif;'>");
                                emailContent.append(
                                                "<div style='max-width: 650px; margin: 30px auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 6px 18px rgba(0,0,0,0.1); overflow: hidden;'>");

                                // Header với logo và tiêu đề
                                emailContent.append(
                                                "<div style='background-color: #1a5276; padding: 20px; text-align: center; color: #ffffff;'>");
                                emailContent.append(
                                                "<div style='display: flex; align-items: center; justify-content: center;'>");
                                emailContent.append(
                                                "<img src='https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiFiXwQ_maatzhJ8oajcgfHJc1mpwv1UDv_pWLFT53HHtUaRZkvOQcuhofX5tamuITa9dxqKQI5wJQ3e3wNqnn1K7sxwdMjGJHY8E_BLsizh8quZdyWs9SX0elWyXJN3Aps-bxHI0wvGvahJapqjJaBJBPXvg5fHqewoiz0RU7ApdukKvi9kbdbSxzPtbqI/s1628/logo%20new%20(1).png' style='height: 40px; margin-right: 15px; display: inline-block; vertical-align: middle;' alt='Zentrix Logo'/>");
                                emailContent.append(
                                                "<div style='display: inline-block; vertical-align: middle; text-align: left;'>");
                                emailContent.append(
                                                "<h1 style='margin: 0; font-size: 28px; font-weight: 700; color: #ffffff;'>Zentrix Store</h1>");
                                emailContent.append(
                                                "<p style='margin: 5px 0 0; font-size: 14px; opacity: 0.9;'>Your Trusted Tech Partner</p>");
                                emailContent.append("</div>");
                                emailContent.append("</div>");
                                emailContent.append("</div>");

                                // Thông báo biên nhận bảo hành
                                emailContent.append(
                                                "<div style='background-color: #e8f4f8; padding: 15px; text-align: center; border-bottom: 1px solid #e0e0e0;'>");
                                emailContent.append(
                                                "<h2 style='color: #1a5276; font-size: 22px; margin: 0; font-weight: 600;'>Warranty Receipt</h2>");
                                emailContent.append(
                                                "<p style='color: #555555; font-size: 13px; margin: 5px 0 0;'>Warranty #")
                                                .append(savedWarranty.getWarnId())
                                                .append(" | ")
                                                .append(savedWarranty.getWarnStartDate() != null
                                                                ? new SimpleDateFormat("dd/MM/yyyy").format(
                                                                                savedWarranty.getWarnStartDate())
                                                                : "N/A")
                                                .append("</p>");
                                emailContent.append("</div>");

                                // Thông tin khách hàng
                                emailContent.append("<div style='padding: 25px 30px;'>");
                                emailContent.append(
                                                "<h3 style='color: #333333; font-size: 20px; margin: 0 0 15px; font-weight: 600; border-bottom: 2px solid #1a5276; padding-bottom: 5px;'>Customer Information</h3>");
                                emailContent.append(
                                                "<p style='margin: 8px 0; font-size: 15px; color: #555555; line-height: 1.6;'><strong style='color: #333333; display: inline-block; width: 120px;'>Name:</strong> ")
                                                .append(fullName != null ? fullName : "N/A").append("</p>");
                                emailContent.append(
                                                "<p style='margin: 8px 0; font-size: 15px; color: #555555; line-height: 1.6;'><strong style='color: #333333; display: inline-block; width: 120px;'>Phone:</strong> ")
                                                .append(phone != null ? phone : "N/A").append("</p>");
                                emailContent.append(
                                                "<p style='margin: 8px 0; font-size: 15px; color: #555555; line-height: 1.6;'><strong style='color: #333333; display: inline-block; width: 120px;'>Email:</strong> <a href='mailto:")
                                                .append(userEmail)
                                                .append("' style='color: #1a5276; text-decoration: none;'>")
                                                .append(userEmail).append("</a></p>");
                                emailContent.append("</div>");

                                // Thông tin bảo hành
                                emailContent.append("<div style='padding: 25px 30px; border-top: 1px solid #e0e0e0;'>");
                                emailContent.append(
                                                "<h3 style='color: #333333; font-size: 20px; margin: 0 0 15px; font-weight: 600; border-bottom: 2px solid #1a5276; padding-bottom: 5px;'>Warranty Details</h3>");
                                emailContent.append(
                                                "<table style='border-collapse: collapse; width: 100%; font-size: 14px; color: #555555;'>");
                                emailContent.append("<thead>");
                                emailContent.append("<tr style='background-color: #e8f4f8; color: #333333;'>");
                                emailContent.append(
                                                "<th style='padding: 12px 15px; text-align: left; font-weight: 600; border-bottom: 1px solid #e0e0e0; width: 40%;'>Field</th>");
                                emailContent.append(
                                                "<th style='padding: 12px 15px; text-align: left; font-weight: 600; border-bottom: 1px solid #e0e0e0; width: 60%;'>Details</th>");
                                emailContent.append("</tr>");
                                emailContent.append("</thead>");
                                emailContent.append("<tbody>");
                                // Bỏ dòng Warranty ID
                                emailContent.append("<tr>");
                                emailContent.append(
                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Product</td>");
                                emailContent.append(
                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; font-weight: 500;'>")
                                                .append(savedWarranty.getProdTypeId() != null
                                                                ? savedWarranty.getProdTypeId().getProdTypeName()
                                                                : "N/A")
                                                .append("</td>");
                                emailContent.append("</tr>");
                                emailContent.append("<tr style='background-color: #fafafa;'>");
                                emailContent.append(
                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Start Date</td>");
                                emailContent.append(
                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; font-weight: 500;'>")
                                                .append(savedWarranty.getWarnStartDate() != null
                                                                ? new SimpleDateFormat("dd/MM/yyyy").format(
                                                                                savedWarranty.getWarnStartDate())
                                                                : "N/A")
                                                .append("</td>");
                                emailContent.append("</tr>");
                                emailContent.append("<tr>");
                                emailContent.append(
                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>End Date</td>");
                                emailContent.append(
                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; font-weight: 500;'>")
                                                .append(savedWarranty.getWarnEndDate() != null
                                                                ? new SimpleDateFormat("dd/MM/yyyy").format(
                                                                                savedWarranty.getWarnEndDate())
                                                                : "N/A")
                                                .append("</td>");
                                emailContent.append("</tr>");
                                emailContent.append("<tr style='background-color: #fafafa;'>");
                                emailContent.append(
                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Warranty Period</td>");
                                emailContent.append(
                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; font-weight: 500;'>")
                                                .append(calculateWeeksDuration(savedWarranty)).append(" weeks")
                                                .append("</td>");
                                emailContent.append("</tr>");
                                emailContent.append("<tr>");
                                emailContent.append(
                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Description</td>");
                                emailContent.append(
                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; font-weight: 500;'>")
                                                .append(savedWarranty.getDescription() != null
                                                                ? savedWarranty.getDescription()
                                                                : "N/A")
                                                .append("</td>");
                                emailContent.append("</tr>");
                                emailContent.append("<tr style='background-color: #fafafa;'>");
                                emailContent.append(
                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Received Items</td>");
                                emailContent.append(
                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; font-weight: 500;'>")
                                                .append(savedWarranty.getReceive() != null ? savedWarranty.getReceive()
                                                                : "N/A")
                                                .append("</td>");
                                emailContent.append("</tr>");
                                emailContent.append("</tbody>");
                                emailContent.append("</table>");
                                emailContent.append("</div>");

                                // Thông tin đính kèm
                                emailContent.append(
                                                "<div style='padding: 20px 30px; border-top: 1px solid #e0e0e0; background-color: #f9f9f9;'>");
                                emailContent.append(
                                                "<p style='margin: 0; font-size: 15px; color: #555555; line-height: 1.6; text-align: center;'><strong style='color: #1a5276;'>Attachment:</strong> Your warranty receipt is attached in PDF format.</p>");
                                emailContent.append(
                                                "<p style='margin: 10px 0 0; font-size: 13px; color: #777777; text-align: center;'>Please bring this receipt when picking up your product.</p>");
                                emailContent.append("</div>");

                                // Lời cảm ơn và footer
                                emailContent.append(
                                                "<div style='padding: 25px 30px; text-align: center; background-color: #1a5276; color: #ffffff; border-radius: 0 0 12px 12px;'>");
                                emailContent.append(
                                                "<p style='margin: 0 0 10px; font-size: 15px; line-height: 1.6;'>Thank you for trusting Zentrix Store!</p>");
                                emailContent.append(
                                                "<p style='margin: 0; font-size: 14px; font-weight: 600;'>Best regards,<br>The Zentrix Team</p>");
                                emailContent.append(
                                                "<p style='margin: 15px 0 0; font-size: 13px; opacity: 0.9;'>Hotline: 0393.510.720 | Email: <a href='mailto:support@zentrix.com' style='color: #ffffff; text-decoration: underline;'>support@zentrix.com</a></p>");
                                emailContent.append("</div>");

                                emailContent.append("</div>");
                                emailContent.append("</body></html>");

                                // Gửi email
                                String subject = "Zentrix Store - Warranty Receipt #" + savedWarranty.getWarnId()
                                                + " | Issued " +
                                                (savedWarranty.getWarnStartDate() != null
                                                                ? new SimpleDateFormat("dd/MM/yyyy").format(
                                                                                savedWarranty.getWarnStartDate())
                                                                : "N/A");
                                emailService.sendEmailWithHtmlBodyAndFilePath(userEmail, null, null, subject,
                                                emailContent.toString(), pdfFilePath);
                                logger.info("Warranty receipt email with PDF sent successfully to: {}", userEmail);
                        } catch (Exception e) {
                                logger.error("Failed to send warranty receipt email with PDF to {}: {}", userEmail,
                                                e.getMessage(), e);
                        } finally {
                                File pdfFile = new File(pdfFilePath);
                                if (pdfFile.exists()) {
                                        pdfFile.delete();
                                        logger.info("Temporary PDF file deleted: {}", pdfFilePath);
                                }
                        }
                } else {
                        logger.warn("User email is null or empty for userId: {}", request.getUserId());
                }

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        private String generateWarrantyPDF(Warranty warranty, String fullName, String phone, Long createdById) {
                String fileName = "warranty_" + warranty.getUserId().getUserId() + "_"
                                + warranty.getProdTypeId().getProdTypeId() + ".pdf";
                String filePath = System.getProperty("java.io.tmpdir") + File.separator + fileName;

                try {
                        PdfWriter writer = new PdfWriter(filePath);
                        PdfDocument pdf = new PdfDocument(writer);
                        Document document = new Document(pdf);
                        document.setFontSize(11);

                        // Tiêu đề
                        document.add(new Paragraph("Warranty Receipt")
                                        .setFontSize(18)
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER));

                        // Thông tin công ty
                        document.add(new Paragraph(
                                        "ZENTRIX INFORMATION TECHNOLOGY SOLUTIONS TRADING SERVICES COMPANY LIMITED"));
                        document.add(new Paragraph(
                                        "Address: 600, Nguyen Van Cu Street, An Binh Ward, Ninh Kieu District, Cantho City, Vietnam"));
                        document.add(new Paragraph("Hotline: 0393.510.720"));

                        // Đường kẻ ngang
                        LineSeparator line = new LineSeparator(new SolidLine());
                        line.setWidth(182); // 196 - 14 = 182 (tương ứng với jsPDF: line(14, 45, 196, 45))
                        document.add(line);

                        // Tiêu đề phụ
                        document.add(new Paragraph("REPAIR RECEIPT - WARRANTY")
                                        .setFontSize(14)
                                        .setBold()
                                        .setMarginTop(10));

                        // Thông tin bảo hành
                        document.add(new Paragraph("Customer: " + (fullName != null ? fullName : "N/A"))
                                        .setMarginTop(5));
                        document.add(new Paragraph("Phone: " + (phone != null ? phone : "N/A"))
                                        .setMarginTop(5));
                        document.add(new Paragraph("Product: "
                                        + (warranty.getProdTypeId() != null ? warranty.getProdTypeId().getProdTypeName()
                                                        : "N/A"))
                                        .setMarginTop(5));
                        document.add(new Paragraph("Description: "
                                        + (warranty.getDescription() != null ? warranty.getDescription() : "N/A"))
                                        .setMarginTop(5));
                        document.add(new Paragraph("Received Items: "
                                        + (warranty.getReceive() != null ? warranty.getReceive() : "N/A"))
                                        .setMarginTop(5));
                        document.add(new Paragraph("Start date: "
                                        + new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()))
                                        .setMarginTop(5));
                        document.add(new Paragraph("Warranty period: " + calculateWeeksDuration(warranty) + " weeks")
                                        .setMarginTop(5));

                        // Ghi chú
                        document.add(new Paragraph("NOTE:")
                                        .setFontSize(10)
                                        .setBold()
                                        .setMarginTop(10));
                        document.add(new Paragraph("NOT RECEIVED CUSTOMER INFORMATION")
                                        .setFontSize(10)
                                        .setMarginTop(5));
                        document.add(new Paragraph("Bring the repair receipt when picking up the device.")
                                        .setFontSize(10)
                                        .setMarginTop(5));
                        document.add(new Paragraph("Warranty period from 10-15 days excluding holidays")
                                        .setFontSize(10)
                                        .setMarginTop(5));

                        // Đường kẻ ngang
                        document.add(line);

                        document.add(new Paragraph("Technician")
                                        .setFontSize(10)
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setMarginTop(-10)); // Đặt cùng dòng với "CUSTOMER SIGN FOR EQUIPMENT"

                        // Footer
                        document.add(new Paragraph("ZENSTIX.STORE")
                                        .setFontSize(9)
                                        .setFontColor(ColorConstants.RED)
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setMarginTop(10));
                        // Lấy thông tin Created by từ createdById
                        String createdByFullName = "N/A";
                        if (createdById != null) {
                                try {
                                        User createdBy = userService.findUserByUserId(createdById);
                                        createdByFullName = (createdBy.getFirstName() != null ? createdBy.getFirstName()
                                                        : "") + " " +
                                                        (createdBy.getLastName() != null ? createdBy.getLastName()
                                                                        : "");
                                        createdByFullName = createdByFullName.trim();
                                        if (createdByFullName.isEmpty()) {
                                                createdByFullName = "N/A";
                                        }
                                } catch (Exception e) {
                                        logger.error("Failed to fetch createdBy user info for userId {}: {}",
                                                        createdById, e.getMessage());
                                }
                        }
                        document.add(new Paragraph("Created by: " + createdByFullName)
                                        .setFontSize(9)
                                        .setFontColor(ColorConstants.RED)
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setMarginTop(5));
                        document.add(new Paragraph("Address: 600, Nguyen Van Cu Street, An Binh Ward,")
                                        .setFontSize(9)
                                        .setFontColor(ColorConstants.RED)
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setMarginTop(5));
                        document.add(new Paragraph("Ninh Kieu District, Cantho City, Vietnam")
                                        .setFontSize(9)
                                        .setFontColor(ColorConstants.RED)
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setMarginTop(5));
                        document.add(new Paragraph("HOTLINE: 0123456789")
                                        .setFontSize(9)
                                        .setFontColor(ColorConstants.RED)
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setMarginTop(5));
                        document.add(new Paragraph("RECEIVED") // Sửa "RECEIVE" thành "RECEIVED"
                                        .setFontSize(9)
                                        .setFontColor(ColorConstants.RED)
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setMarginTop(5));

                        document.close();
                        logger.info("PDF generated successfully: {}", filePath);
                } catch (Exception e) {
                        logger.error("Failed to generate PDF: {}", e.getMessage(), e);
                        throw new RuntimeException("Failed to generate warranty PDF", e);
                }

                return filePath;
        }

        private int calculateWeeksDuration(Warranty warranty) {
                if (warranty.getWarnStartDate() != null && warranty.getWarnEndDate() != null) {
                        long diffInMillies = Math.abs(
                                        warranty.getWarnEndDate().getTime() - warranty.getWarnStartDate().getTime());
                        return (int) Math.ceil(diffInMillies / (1000.0 * 60 * 60 * 24 * 7));
                }
                return 0;
        }

        /**
         * This method allows to update an existing warranty by ID and status with
         * authorization for SELLER STAFF or ADMIN roles
         * 
         * @param warrantyRequest request body containing updated warranty details
         * @param id              ID of the warranty to update
         * @param status          new status value for the warranty
         * @return ResponseEntity containing the updated warranty and response metadata
         */
        @PreAuthorize("hasAnyRole('SELLER STAFF', 'ADMIN')")
        @Operation(summary = "Update a warranty", description = "Updates an existing warranty record by ID and status.", security = @SecurityRequirement(name = "Authorization"))
        @PutMapping("/{id}/{status}")
        public ResponseEntity<ResponseObject<Warranty>> updateWarranty(
                        @RequestBody WarrantyRequest warrantyRequest,
                        @Parameter(description = "ID of the warranty to update", required = true) @PathVariable Long id,
                        @Parameter(description = "New status value (e.g., 1 for active, 2 for done)", required = true) @PathVariable int status) {
                warrantyRequest.setStatus(status);
                Warranty updatedWarranty = warrantyService.updateWarranty(warrantyRequest, id);
                ResponseObject<Warranty> response = new ResponseObject.Builder<Warranty>()
                                .content(updatedWarranty)
                                .message("Warranty updated successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();

                // Lấy warranty hiện tại từ ID
                Warranty existingWarranty = warrantyService.findWarrantyById(id);
                if (existingWarranty != null && existingWarranty.getUserId() != null) {
                        Long userId = existingWarranty.getUserId().getUserId();
                        String userEmail = userService.findUserByUserId(userId).getEmail();
                        System.out.println(
                                        "=====================================================================================");
                        System.out.println("User email: " + userEmail);

                        if (userEmail != null && !userEmail.isEmpty()) {
                                try {
                                        if (status == 2) { // Giả sử 2 là "Has Done" dựa trên frontend
                                                StringBuilder emailContent = new StringBuilder();
                                                emailContent.append(
                                                                "<!DOCTYPE html><html lang='en'><body style='margin: 0; padding: 0; background-color: #f4f4f4; font-family: Arial, sans-serif;'>");
                                                emailContent.append(
                                                                "<div style='max-width: 650px; margin: 30px auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 6px 18px rgba(0,0,0,0.1); overflow: hidden;'>");

                                                // Header với logo và tiêu đề
                                                emailContent.append(
                                                                "<div style='background-color: #1a5276; padding: 20px; text-align: center; color: #ffffff;'>");
                                                emailContent.append(
                                                                "<div style='display: flex; align-items: center; justify-content: center;'>");
                                                emailContent.append(
                                                                "<img src='https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiFiXwQ_maatzhJ8oajcgfHJc1mpwv1UDv_pWLFT53HHtUaRZkvOQcuhofX5tamuITa9dxqKQI5wJQ3e3wNqnn1K7sxwdMjGJHY8E_BLsizh8quZdyWs9SX0elWyXJN3Aps-bxHI0wvGvahJapqjJaBJBPXvg5fHqewoiz0RU7ApdukKvi9kbdbSxzPtbqI/s1628/logo%20new%20(1).png' style='height: 40px; margin-right: 15px; display: inline-block; vertical-align: middle;' alt='Zentrix Logo'/>");
                                                emailContent.append(
                                                                "<div style='display: inline-block; vertical-align: middle; text-align: left;'>");
                                                emailContent.append(
                                                                "<h1 style='margin: 0; font-size: 28px; font-weight: 700; color: #ffffff;'>Zentrix Store</h1>");
                                                emailContent.append(
                                                                "<p style='margin: 5px 0 0; font-size: 14px; opacity: 0.9;'>Your Trusted Tech Partner</p>");
                                                emailContent.append("</div>");
                                                emailContent.append("</div>");
                                                emailContent.append("</div>");

                                                // Thông báo hoàn tất bảo hành
                                                emailContent.append(
                                                                "<div style='background-color: #e8f4f8; padding: 15px; text-align: center; border-bottom: 1px solid #e0e0e0;'>");
                                                emailContent.append(
                                                                "<h2 style='color: #1a5276; font-size: 22px; margin: 0; font-weight: 600;'>Warranty Completion</h2>");
                                                emailContent.append(
                                                                "<p style='color: #555555; font-size: 13px; margin: 5px 0 0;'>Your product is ready for pickup!</p>");
                                                emailContent.append("</div>");

                                                // Thông tin khách hàng
                                                String fullName = userService.findUserByUserId(userId).getFirstName()
                                                                + " " +
                                                                userService.findUserByUserId(userId).getLastName();
                                                String phone = userService.findUserByUserId(userId).getPhone();
                                                emailContent.append("<div style='padding: 25px 30px;'>");
                                                emailContent.append(
                                                                "<h3 style='color: #333333; font-size: 20px; margin: 0 0 15px; font-weight: 600; border-bottom: 2px solid #1a5276; padding-bottom: 5px;'>Customer Information</h3>");
                                                emailContent.append(
                                                                "<p style='margin: 8px 0; font-size: 15px; color: #555555; line-height: 1.6;'><strong style='color: #333333; display: inline-block; width: 120px;'>Name:</strong> ")
                                                                .append(fullName != null ? fullName : "N/A")
                                                                .append("</p>");
                                                emailContent.append(
                                                                "<p style='margin: 8px 0; font-size: 15px; color: #555555; line-height: 1.6;'><strong style='color: #333333; display: inline-block; width: 120px;'>Phone:</strong> ")
                                                                .append(phone != null ? phone : "N/A").append("</p>");
                                                emailContent.append(
                                                                "<p style='margin: 8px 0; font-size: 15px; color: #555555; line-height: 1.6;'><strong style='color: #333333; display: inline-block; width: 120px;'>Email:</strong> <a href='mailto:")
                                                                .append(userEmail)
                                                                .append("' style='color: #1a5276; text-decoration: none;'>")
                                                                .append(userEmail).append("</a></p>");
                                                emailContent.append("</div>");

                                                // Thông tin bảo hành (chỉ giữ Product, Description, Received Items)
                                                emailContent.append(
                                                                "<div style='padding: 25px 30px; border-top: 1px solid #e0e0e0;'>");
                                                emailContent.append(
                                                                "<h3 style='color: #333333; font-size: 20px; margin: 0 0 15px; font-weight: 600; border-bottom: 2px solid #1a5276; padding-bottom: 5px;'>Warranty Details</h3>");
                                                emailContent.append(
                                                                "<table style='border-collapse: collapse; width: 100%; font-size: 14px; color: #555555;'>");
                                                emailContent.append("<thead>");
                                                emailContent.append(
                                                                "<tr style='background-color: #e8f4f8; color: #333333;'>");
                                                emailContent.append(
                                                                "<th style='padding: 12px 15px; text-align: left; font-weight: 600; border-bottom: 1px solid #e0e0e0; width: 40%;'>Field</th>");
                                                emailContent.append(
                                                                "<th style='padding: 12px 15px; text-align: left; font-weight: 600; border-bottom: 1px solid #e0e0e0; width: 60%;'>Details</th>");
                                                emailContent.append("</tr>");
                                                emailContent.append("</thead>");
                                                emailContent.append("<tbody>");
                                                // Product
                                                emailContent.append("<tr>");
                                                emailContent.append(
                                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Product</td>");
                                                emailContent.append(
                                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; font-weight: 500;'>")
                                                                .append(existingWarranty.getProdTypeId() != null
                                                                                ? existingWarranty.getProdTypeId()
                                                                                                .getProdTypeName()
                                                                                : "N/A")
                                                                .append("</td>");
                                                emailContent.append("</tr>");
                                                // Description
                                                emailContent.append("<tr style='background-color: #fafafa;'>");
                                                emailContent.append(
                                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Description</td>");
                                                emailContent.append(
                                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; font-weight: 500;'>")
                                                                .append(existingWarranty.getDescription() != null
                                                                                ? existingWarranty.getDescription()
                                                                                : "N/A")
                                                                .append("</td>");
                                                emailContent.append("</tr>");
                                                // Received Items
                                                emailContent.append("<tr>");
                                                emailContent.append(
                                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Received Items</td>");
                                                emailContent.append(
                                                                "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; font-weight: 500;'>")
                                                                .append(existingWarranty.getReceive() != null
                                                                                ? existingWarranty.getReceive()
                                                                                : "N/A")
                                                                .append("</td>");
                                                emailContent.append("</tr>");
                                                emailContent.append("</tbody>");
                                                emailContent.append("</table>");
                                                emailContent.append("</div>");

                                                // Thông tin nhận sản phẩm
                                                emailContent.append(
                                                                "<div style='padding: 20px 30px; border-top: 1px solid #e0e0e0; background-color: #f9f9f9;'>");
                                                emailContent.append(
                                                                "<p style='margin: 0; font-size: 15px; color: #555555; line-height: 1.6; text-align: center;'><strong style='color: #1a5276;'>Your product is ready!</strong> Please bring your warranty receipt to pick up your product at:</p>");
                                                emailContent.append(
                                                                "<p style='margin: 10px 0 0; font-size: 13px; color: #777777; text-align: center;'>600, Nguyen Van Cu Street, An Binh Ward, Ninh Kieu District, Cantho City, Vietnam</p>");
                                                emailContent.append("</div>");

                                                // Footer
                                                emailContent.append(
                                                                "<div style='padding: 25px 30px; text-align: center; background-color: #1a5276; color: #ffffff; border-radius: 0 0 12px 12px;'>");
                                                emailContent.append(
                                                                "<p style='margin: 0 0 10px; font-size: 15px; line-height: 1.6;'>Thank you for trusting Zentrix Store!</p>");
                                                emailContent.append(
                                                                "<p style='margin: 0; font-size: 14px; font-weight: 600;'>Best regards,<br>The Zentrix Team</p>");
                                                emailContent.append(
                                                                "<p style='margin: 15px 0 0; font-size: 13px; opacity: 0.9;'>Hotline: 0393.510.720 | Email: <a href='mailto:support@zentrix.com' style='color: #ffffff; text-decoration: underline;'>support@zentrix.com</a></p>");
                                                emailContent.append("</div>");

                                                emailContent.append("</div>");
                                                emailContent.append("</body></html>");

                                                // Gửi email
                                                String subject = "Zentrix Store - Warranty Completion | Ready for Pickup";
                                                emailService.sendEmailWithHtmlBody(userEmail, null, null, subject,
                                                                emailContent.toString());
                                                logger.info("Warranty completion email sent successfully to: {}",
                                                                userEmail);
                                        }
                                } catch (Exception e) {
                                        logger.error("Failed to send email to {}: {}", userEmail, e.getMessage(), e);
                                }
                        } else {
                                logger.warn("User email is null or empty for userId: {}", userId);
                        }
                } else {
                        logger.warn("Warranty not found or userId is null for warranty ID: {}", id);
                }

                return ResponseEntity.ok(response);
        }

        /**
         * This method allows to retrieve a warranty by its ID with authorization for
         * SELLER STAFF or ADMIN roles
         * 
         * @param id ID of the warranty to retrieve
         * @return ResponseEntity containing the warranty or a not found response
         */
        @PreAuthorize("hasAnyRole('SELLER STAFF', 'ADMIN')")
        @Operation(summary = "Get warranty by ID", description = "Retrieves a warranty by its unique ID.", security = @SecurityRequirement(name = "Authorization"))
        @GetMapping("/{id}")
        public ResponseEntity<ResponseObject<Warranty>> getWarrantyById(
                        @Parameter(description = "ID of the warranty to retrieve", required = true) @PathVariable Long id) {
                Warranty warranty = warrantyService.findWarrantyById(id);
                if (warranty != null) {
                        ResponseObject<Warranty> response = new ResponseObject.Builder<Warranty>()
                                        .content(warranty)
                                        .message("Warranty retrieved successfully")
                                        .code(HttpStatus.OK.value())
                                        .success(true)
                                        .build();
                        return ResponseEntity.ok(response);
                }
                ResponseObject<Warranty> response = new ResponseObject.Builder<Warranty>()
                                .content(null)
                                .message("Warranty not found")
                                .code(HttpStatus.NOT_FOUND.value())
                                .success(false)
                                .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        /**
         * This method allows to retrieve all warranties with pagination and sorting for
         * SELLER STAFF or ADMIN roles
         * 
         * @param page    page number (default: 0)
         * @param size    number of items per page (default: 10)
         * @param sortBy  field to sort by (default: "warnId")
         * @param sortDir sort direction (default: "asc")
         * @return ResponseEntity containing the paginated list of warranties
         */
        @PreAuthorize("hasAnyRole('SELLER STAFF', 'ADMIN')")
        @Operation(summary = "Get all warranties with pagination", description = "Fetches a paginated and sorted list of all warranties from the database.", security = @SecurityRequirement(name = "Authorization"))
        @GetMapping
        public ResponseEntity<ResponseObject<Page<Warranty>>> getAllWarranties(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "warnId") String sortBy,
                        @RequestParam(defaultValue = "asc") String sortDir) {
                Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<Warranty> warranties = warrantyService.findAllWarranty(pageable);
                ResponseObject<Page<Warranty>> response = new ResponseObject.Builder<Page<Warranty>>()
                                .content(warranties)
                                .message("All warranties retrieved successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * This method allows to retrieve warranties by user's phone number with
         * pagination
         * 
         * @param phone   phone number of the user
         * @param page    page number (default: 0)
         * @param size    number of items per page (default: 10)
         * @param sortBy  field to sort by (default: "warnId")
         * @param sortDir sort direction (default: "asc")
         * @return ResponseEntity containing the paginated list of warranties or error
         *         response
         */
        @PreAuthorize("hasRole('CUSTOMER') or hasRole('SELLER STAFF') or hasRole('ADMIN')")
        @Operation(summary = "Get warranties by user phone with pagination", description = "Fetches a paginated and sorted list of warranties by user's phone number.", security = @SecurityRequirement(name = "Authorization"))
        @GetMapping("/phone")
        public ResponseEntity<ResponseObject<Page<Warranty>>> findWarrantiesByUserPhone(
                        @Parameter(description = "Phone number of the user to search for", required = true) @RequestParam String phone,
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field (e.g., warnId)", example = "warnId") @RequestParam(defaultValue = "warnId") String sortBy,
                        @Parameter(description = "Sort direction (asc or desc)", example = "asc") @RequestParam(defaultValue = "asc") String sortDir) {
                try {
                        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
                        Pageable pageable = PageRequest.of(page, size, sort);
                        Page<Warranty> warranties = warrantyService.findWarrantyByUserPhone(phone, pageable);
                        ResponseObject<Page<Warranty>> response = new ResponseObject.Builder<Page<Warranty>>()
                                        .content(warranties)
                                        .message("Warranties retrieved successfully by user phone")
                                        .code(HttpStatus.OK.value())
                                        .success(true)
                                        .build();
                        return ResponseEntity.ok(response);
                } catch (ActionFailedException e) {
                        ResponseObject<Page<Warranty>> response = new ResponseObject.Builder<Page<Warranty>>()
                                        .content(null)
                                        .message("No warranties found for phone number: " + phone)
                                        .code(HttpStatus.NOT_FOUND.value())
                                        .success(false)
                                        .build();
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
        }

        /**
         * This method allows to delete a warranty by its ID with authorization for
         * SELLER STAFF or ADMIN roles
         * 
         * @param id ID of the warranty to delete
         * @return ResponseEntity containing success response metadata
         */
        @PreAuthorize("hasAnyRole('SELLER STAFF', 'ADMIN')")
        @Operation(summary = "Delete a warranty", description = "Deletes a warranty by its ID.", security = @SecurityRequirement(name = "Authorization"))
        @DeleteMapping("/{id}")
        public ResponseEntity<ResponseObject<Void>> deleteWarranty(
                        @Parameter(description = "ID of the warranty to delete", required = true) @PathVariable Long id) {
                warrantyService.deleteWarranty(id);
                ResponseObject<Void> response = new ResponseObject.Builder<Void>()
                                .content(null)
                                .message("Warranty deleted successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }
}