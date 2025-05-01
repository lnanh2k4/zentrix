package com.zentrix.model.request;


import java.util.Date;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class NotificationRequest {
     @NotNull(message = "Creator must not be null")
     @Min(value = 1, message = "Creator must be greater than 0")
     Long createdById;

     @NotBlank(message = "Title cannot be blank")
     @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
     String title;

     @NotBlank(message = "Description cannot be blank")
     @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
     String description;

     @NotNull(message = "Created date must not be null")
     @PastOrPresent(message = "Created date cannot be in the future")
     @Temporal(TemporalType.TIMESTAMP)
     Date createdAt;

     @NotNull(message = "Status must not be null")
     @Min(value = 0, message = "Status must be 0 (inactive) or 1 (active)")
     @Max(value = 1, message = "Status must be 0 (inactive) or 1 (active)")
     Integer status;

     Long orderId;
}
