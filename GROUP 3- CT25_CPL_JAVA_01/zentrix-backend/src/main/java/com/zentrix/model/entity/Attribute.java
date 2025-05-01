package com.zentrix.model.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

/*
* @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Attribute {
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tối ưu auto-increment
    Long atbId;

    // Attribute
    @Column(nullable = false, length = 255)
    String atbName;

    // Foreign Key - Quan hệ 1-N với ProductTypeAttribute
    @OneToMany(mappedBy = "atbId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<ProductTypeAttribute> productTypeAttributes;
}
