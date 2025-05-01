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

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 18, 2025
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeAttributeRequest {

    @NotNull(message = "Product type ID cannot be null")
    @Min(value = 1, message = "Product type ID must be a positive integer")
    Long prodTypeId;

    @NotNull(message = "Attribute ID cannot be null")
    @Min(value = 1, message = "Attribute ID must be a positive integer")
    Long atbId;

    @Size(max = 100, message = "Product attribute value cannot exceed 100 characters")
    String prodAtbValue;

    @Size(max = 500, message = "Attribute description cannot exceed 500 characters")
    String atbDescription;
}