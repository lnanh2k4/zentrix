package com.zentrix.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @NotNull(message = "Current Password can not be null!")
    String currentPassword;
    @NotNull(message = "New Password can not be null!")
    String newPassword;
}
