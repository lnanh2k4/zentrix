package com.zentrix.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date April 02, 2025
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchRequest {
    @NotBlank(message = "Branch name cannot be blank")
    @Size(max = 255, message = "Branch name cannot exceed 255 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Branch name can only contain letters, numbers, and spaces")
    private String brchName;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    private String phone; 

    @NotNull(message = "Status cannot be null")
    private Integer status;
}
