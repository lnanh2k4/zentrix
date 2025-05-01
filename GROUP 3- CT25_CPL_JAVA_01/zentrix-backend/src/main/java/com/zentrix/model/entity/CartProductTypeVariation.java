package com.zentrix.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.JsonIOException;

import io.swagger.v3.core.util.Json;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartProductTypeVariation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    Long cartProductTypeVariationId;
    @ManyToOne

    Cart cartId;
    @ManyToOne
    ProductTypeVariation prodTypeVariId;

    Integer quantity;

    String variCode;
}
