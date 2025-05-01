package com.zentrix.model.request;

import java.util.Date;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Dang Cong Khanh - CE180117 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {
    Long createdBy;
    Long approvedBy;

    @NotNull(message = "Promotion name must not be null")
    @Size(min = 1, max = 255, message = "Promotion name must be between 1 and 255 characters")
    @Pattern(regexp = "^[\\p{L}\\s_]{1,255}$", message = "Promotion name must contain only letters (with or without accents), spaces, and '_', no other special characters")
    String promName;

    @NotNull(message = "Promotion code must not be null")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Promotion code can only contain letters, numbers, and underscores (no spaces or special characters)")
    String promCode;

    @NotNull(message = "Discount must not be null")
    @DecimalMin(value = "1.0", message = "Discount must be at least 1")
    @DecimalMax(value = "100.0", message = "Discount must not exceed 100")
    Float discount;

    @NotNull(message = "Start date must not be null")
    Date startDate;

    @NotNull(message = "End date must not be null")
    @Future(message = "End date must be in the future")
    Date endDate;

    @NotNull(message = "Quantity must not be null")
    @Min(value = 1, message = "Quantity must be 1 or greater")
    Integer quantity;
}