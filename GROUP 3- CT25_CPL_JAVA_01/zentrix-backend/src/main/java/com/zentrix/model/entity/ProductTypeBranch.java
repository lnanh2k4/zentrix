package com.zentrix.model.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductTypeBranch {
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long prodTypeBrchId;

    // Foreign Keys
    @ManyToOne
    ProductType prodTypeId;

    @ManyToOne
    Branch brchId;

    // Attributes
    Integer quantity;

    Integer status;

    @OneToMany(mappedBy = "prodTypeBranchId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<OrderDetail> orderDetails;

    @Transient
    private String statusMessage;
}
