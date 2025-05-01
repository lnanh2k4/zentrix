package com.zentrix.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 12, 2025
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipRequest {
    // Attribute
    @NotBlank(message = "Name must be not blank")
    String mbsName;
    @NotBlank(message = "Name must be not blank")
    @PositiveOrZero(message = "Point must be an integer number and greater than or equal 0")
    Long mbsPoint;
    String mbsDescription;
}
