package com.zentrix.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class StockDetail {
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long stockDetailId;

    // Foreign Key
    @ManyToOne
    @JsonIgnore
    Stock stockId;

    @ManyToOne
    ProductTypeBranch prodTypeBrchId;

    // Attributes
    Integer stockQuantity;

    Double importPrice;
}
