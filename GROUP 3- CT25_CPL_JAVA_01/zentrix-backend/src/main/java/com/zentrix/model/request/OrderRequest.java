package com.zentrix.model.request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class OrderRequest {

    // Foreign Key

    Long userId;

    Long promId;

    Long brchId;
    // Attribute
    @NotBlank(message = "Address must not be blank")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    String address;
    Integer status;
    @NotBlank(message = "Payment method must not be blank")
    String paymentMethod;
    LocalDateTime createdAt;
    List<OrderDetailRequest> orderDetailRequests;
}
