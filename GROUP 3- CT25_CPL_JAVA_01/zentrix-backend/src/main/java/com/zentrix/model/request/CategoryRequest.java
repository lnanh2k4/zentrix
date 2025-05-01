package com.zentrix.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

/*
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date April 02, 2025
 */

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class CategoryRequest {

    @NotBlank(message = "Category name cannot be blank")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String cateName;  

    @Min(value = 0, message = "Parent category ID must be a non-negative integer")
    private Integer parentCateId;  
}