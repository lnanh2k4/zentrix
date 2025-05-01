package com.zentrix.model.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 18, 2025
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeBranchRequest {

    @NotNull(message = "Product type ID cannot be null")
    @Min(value = 1, message = "Product type ID must be a positive integer")
    Long prodTypeId;

    @NotNull(message = "Branch ID cannot be null")
    @Min(value = 1, message = "Branch ID must be a positive integer")
    Long brchId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity must be non-negative")
    Integer quantity;
}