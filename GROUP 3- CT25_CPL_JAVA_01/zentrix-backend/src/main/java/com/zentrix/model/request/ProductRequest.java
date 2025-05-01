package com.zentrix.model.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotNull(message = "Category ID cannot be null")
    @Min(value = 1, message = "Category ID must be a positive integer")
    Integer cateId;

    @NotNull(message = "Supplier ID cannot be null")
    @Min(value = 1, message = "Supplier ID must be a positive integer")
    Integer suppId;

    @NotBlank(message = "Product name cannot be blank")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    String prodName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description;

    @Min(value = 0, message = "VAT must be non-negative")
    Float vat;
}