package com.zentrix.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
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

/*
* @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetail {
    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long ordtId;
    // Foreign Key
    @ManyToOne
    @JsonIgnore
    Order orderId;
    @ManyToOne
    ProductTypeBranch prodTypeBranchId;
    // Attribute
    @Column(nullable = false)
    Integer quantity;
    @Column(nullable = false)
    Integer unitPrice;
    @Column(nullable = false)
    Float amountNotVat;
    String variation;
    @Column(nullable = false)
    Float vatRate;
}
