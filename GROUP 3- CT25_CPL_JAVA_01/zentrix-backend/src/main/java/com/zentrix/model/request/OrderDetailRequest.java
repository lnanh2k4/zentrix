package com.zentrix.model.request;

import com.zentrix.model.entity.ProductTypeBranch;

import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date April 06, 2025
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class OrderDetailRequest {
    Long orderId;

    Long prodTypeBranchId;
    // Attribute
    @NotNull(message = "Quantity must not be null")
    @Positive(message = "Quantity must be a positive number")
    Integer quantity;
    @NotNull(message = "Unit price must not be null")
    @Positive(message = "Unit price must be a positive number")
    Integer unitPrice;
    String variation;
    @NotNull(message = "Amount (excluding VAT) must not be null")
    @Positive(message = "Amount (excluding VAT) must be a positive number")
    Float amountNotVat;
    @NotNull(message = "VAT rate must not be null")
    @Positive(message = "VAT rate must be a positive number")
    Float vatRate;
}
