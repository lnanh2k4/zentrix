package com.zentrix.model.request;

import java.util.Date;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Dang Cong Khanh - CE180117 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class WarrantyRequest {

    Long userId;
    Long prodTypeId;
    Long createdBy;

    @NotNull(message = "Warranty start date must not be null")
    Date warnStartDate;

    @NotNull(message = "Warranty end date must not be null")
    @Future(message = "Warranty end date must be in the future compared to start date")
    Date warnEndDate;

    @Size(min = 1, max = 255, message = "Length of description must be between 1 and 255 characters")
    String description;

    @Size(min = 1, max = 255, message = "Length of  must be between 1 and 255 characters")
    String receive;

    @PositiveOrZero(message = "Status must be an integer number and greater than or equal to 0")
    Integer status;
}