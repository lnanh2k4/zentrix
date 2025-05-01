package com.zentrix.model.request;

import java.time.LocalDate;

import com.zentrix.model.entity.Branch;
import com.zentrix.model.entity.User;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffRequest {
    @Id
    Long staffId;
    Long userId;
    Long mbsId;
    Long brchId;
    Integer roleId;
    @NotBlank(message = "Name must be not blank")
    String username;
    @NotBlank(message = "Password must be not blank")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*(a-z)(?=.*(A-Z)(?=.*[!@#$%^&*()_+-=\\[\\]{}|;:,.<>?](?!.*\\s).{8,}$)))", message = "Password must be at least 8 characters, contain at least 1 number, 1 uppercase letter, 1 lowercase letter, 1 special character and no spaces")
    String password;
    @NotBlank(message = "Email must be not blank")
    @Email(message = "Email is not valid")
    String email;
    @Past(message = "Date of birth must be in the past")
    LocalDate dob;
    @Pattern(regexp = "^[0,9]{1,15}$", message = "Phone number must be between 1 and 15 numbers")
    String phone;
    @Size(min = 1, max = 255, message = "length of firstname must be between 1 and 255 letters")
    @Pattern(regexp = "^[\\p{L}\\s-]{1,255}$", message = "First name must be from 1 to 255 characters, contain only letters (with or without accents), spaces and '-', no other special characters")
    String firstName;
    @Size(min = 1, max = 255, message = "length of lastname must be between 1 and 255 letters")
    @Pattern(regexp = "^[\\p{L}\\s-]{1,255}$", message = "Last name must be from 1 to 255 characters, contain only letters (with or without accents), spaces and '-', no other special characters")
    String lastName;
    String address;
    @PositiveOrZero(message = "Sex must be an integer number and greater than or equal 0")
    Integer sex;
    @Size(min = 1, max = 255, message = "length of name of company must be between 1 and 255 letters")
    String companyName;
    String taxCode;
}
