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
import jakarta.validation.constraints.Max;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 18, 2025
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeVariationRequest {

      @NotNull(message = "Variation ID cannot be null")
      @Min(value = 1, message = "Variation ID must be a positive integer")
      Long variId;

      @NotNull(message = "Product type ID cannot be null")
      @Min(value = 1, message = "Product type ID must be a positive integer")
      Long prodTypeId;

      @Size(max = 100, message = "Product type value cannot exceed 100 characters")
      String prodTypeValue;

      @NotNull(message = "Default variation flag cannot be null")
      @Min(value = 0, message = "Default variation must be 0 or 1")
      @Max(value = 1, message = "Default variation must be 0 or 1")
      Integer defaultVari;
}