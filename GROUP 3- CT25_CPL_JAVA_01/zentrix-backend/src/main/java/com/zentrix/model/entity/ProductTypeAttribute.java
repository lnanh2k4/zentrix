package com.zentrix.model.entity;

import jakarta.persistence.*;
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
public class ProductTypeAttribute {
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long prodAtbId;

    // Foreign Keys
    @ManyToOne
    ProductType prodTypeId;

    @ManyToOne
    Attribute atbId;

    // Attributes

    String prodAtbValue;

    @NotBlank(message = "You must enter this field.")
    String atbDescription;
}
