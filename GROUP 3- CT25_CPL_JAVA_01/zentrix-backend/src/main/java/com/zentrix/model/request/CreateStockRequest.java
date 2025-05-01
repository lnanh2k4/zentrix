package com.zentrix.model.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockRequest {
    @NotNull(message = "Branch ID cannot be null")
    @Min(value = 1, message = "Branch ID must be a positive integer")
    Long brchId;

    @NotNull(message = "Supplier ID cannot be null")
    @Min(value = 1, message = "Supplier ID must be a positive integer")
    Integer supplierId;

    @NotNull(message = "Created by ID cannot be null")
    @Min(value = 1, message = "Created by ID must be a positive integer")
    Long createdBy;

    @NotNull(message = "Import date cannot be null")
    @PastOrPresent(message = "Import date must be in the past or present")
    LocalDate importDate;

    @NotNull(message = "Created at timestamp cannot be null")
    @PastOrPresent(message = "Created at timestamp must be in the past or present")
    LocalDateTime createdAt;
}
