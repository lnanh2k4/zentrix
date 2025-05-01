package com.zentrix.model.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 18, 2025
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ImageProductTypeRequest {
    Long prodTypeId;
    Long imageId;
}
