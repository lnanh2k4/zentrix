package com.zentrix.model.request;

import java.sql.Date;
import org.springframework.web.multipart.MultipartFile;

import com.zentrix.model.utils.Status;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * @date February 12, 2025
 */

    
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class PostRequest {
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description;

    @NotNull(message = "Creator must not be null")
    Long createdBy;

    Long approvedBy;

    @NotNull(message = "Created date must not be null")
    @PastOrPresent(message = "Created date cannot be in the future")
    Date createdAt;

    MultipartFile[] imageFiles;
    String imagePath;

    String[] existingImageLinks;

    @Enumerated(EnumType.STRING)
    Status status; 

}
