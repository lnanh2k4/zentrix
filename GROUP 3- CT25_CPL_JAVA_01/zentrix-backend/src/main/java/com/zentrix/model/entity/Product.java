package com.zentrix.model.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    /* Primary Key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long prodId;
    // Foreign Key
    @NotNull
    @ManyToOne
    Category cateId;

    @NotNull
    @ManyToOne
    Supplier suppId;

    @OneToMany(mappedBy = "prodId", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProductType> productTypes;

    String prodName;

    String description;

    Float vat;

    Integer status;

    public Product(Long id, String prodName, String description, Float vat) {
        this.prodId = id;
        this.prodName = prodName;
        this.description = description;
        this.vat = vat;
    }
}