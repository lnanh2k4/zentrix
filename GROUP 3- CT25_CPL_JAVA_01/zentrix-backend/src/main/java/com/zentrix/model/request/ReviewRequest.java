package com.zentrix.model.request;

import java.sql.Date;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class ReviewRequest {
     @NotNull(message = "Product cannot be null")
     @Positive(message = "Product must be a positive number")
     Long productId;
     @NotNull(message = "User cannot be null")
     @Positive(message = "User must be a positive number")
     Long userId;
     @NotBlank(message = "Comment cannot be blank")
     @Size(max = 1000, message = "Comment must not exceed 1000 characters")
     String comment;
     @NotNull(message = "Rating cannot be null")
     @Min(value = 1, message = "Rating must be between 1 and 5")
     @Max(value = 5, message = "Rating must be between 1 and 5")
     Integer rating;
     @NotNull(message = "Created date cannot be null")
     @PastOrPresent(message = "Created date cannot be in the future")
     Date createdAt;
     MultipartFile imageFile;
}
