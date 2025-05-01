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
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class Variation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Primary Key
    Long variId;

    // Attribute
    String variName;

    @OneToMany(mappedBy = "variId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<ProductTypeVariation> productTypeVariations;
}
