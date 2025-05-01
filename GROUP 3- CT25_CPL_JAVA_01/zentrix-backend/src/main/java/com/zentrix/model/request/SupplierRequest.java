package com.zentrix.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date April 02, 2025
 */

@Data  
@NoArgsConstructor  
@AllArgsConstructor  
public class SupplierRequest {

    @NotBlank(message = "Supplier name cannot be blank")
    @Size(min = 2, max = 100, message = "Supplier name must be between 2 and 100 characters")
    private String suppName;  

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;  

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone number must be 8-15 digits, optionally starting with a '+'")
    private String phone;  

    @NotBlank(message = "Address cannot be blank")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;  
}