package com.zentrix.model.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class ProductTypeVariation {
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long prodTypeVariId;

    // Foreign Keys
    @ManyToOne
    Variation variId;

    @ManyToOne
    ProductType prodTypeId;
    // Attributes
    String prodTypeValue;
    Integer defaultVari;
    @OneToMany(mappedBy = "prodTypeVariId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<CartProductTypeVariation> cartProductTypeVariations;
}
