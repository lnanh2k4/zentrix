package com.zentrix.model.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class ProductTypeRequest {

    @NotNull(message = "Product ID cannot be null")
    @Min(value = 1, message = "Product ID must be a positive integer")
    Long prodId;

    @NotBlank(message = "Product type name cannot be blank")
    @Size(min = 2, max = 100, message = "Product type name must be between 2 and 100 characters")
    String prodTypeName;

    @NotNull(message = "Product type price cannot be null")
    @Min(value = 0, message = "Product type price must be non-negative")
    Double prodTypePrice;

    @NotBlank(message = "Unit cannot be blank")
    @Size(min = 1, max = 20, message = "Unit must be between 1 and 20 characters")
    String unit;

    @NotNull(message = "Unit price cannot be null")
    @Min(value = 0, message = "Unit price must be non-negative")
    Double unitPrice;
}