package com.zentrix.model.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
@NoArgsConstructor
@AllArgsConstructor
public class ProductType {
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long prodTypeId;

    // Foreign Key
    @ManyToOne
    @JsonIgnore
    Product prodId; // FK prod_id

    // Attribute

    String prodTypeName;

    Double prodTypePrice;

    String unit;

    Double unitPrice;

    Integer status;

    @OneToMany(mappedBy = "prodTypeId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<Warranty> warranties;

    @OneToMany(mappedBy = "prodTypeId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<ProductTypeAttribute> productTypeAttributes;

    @OneToMany(mappedBy = "prodTypeId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<ProductTypeVariation> productTypeVariations;

    @OneToMany(mappedBy = "prodTypeId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<ProductTypeBranch> productTypeBranches;

    @OneToMany(mappedBy = "prodTypeId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<ImageProductType> imageProductTypes;
}
