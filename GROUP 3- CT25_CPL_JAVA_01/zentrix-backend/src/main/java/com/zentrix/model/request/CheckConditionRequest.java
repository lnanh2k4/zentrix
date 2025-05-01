package com.zentrix.model.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class CheckConditionRequest {
     @NotNull(message = "Product cannot be null")
     @Positive(message = "Product must be a positive number")
     Long productId;
     @NotNull(message = "User cannot be null")
     @Positive(message = "User must be a positive number")
     Long userId;
}
