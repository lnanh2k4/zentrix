package com.zentrix.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockDetailRequest {
    @NotNull(message = "Stock ID cannot be null")
    @Min(value = 1, message = "Stock ID must be a positive integer")
    Long stockId;

    @NotNull(message = "Product type branch ID cannot be null")
    @Min(value = 1, message = "Product type branch ID must be a positive integer")
    Long prodTypeBrchId;

    @NotNull(message = "Stock quantity cannot be null")
    @Min(value = 0, message = "Stock quantity must be non-negative")
    Integer stockQuantity;

    @NotNull(message = "Import price cannot be null")
    @Min(value = 0, message = "Import price must be non-negative")
    Double importPrice;
}