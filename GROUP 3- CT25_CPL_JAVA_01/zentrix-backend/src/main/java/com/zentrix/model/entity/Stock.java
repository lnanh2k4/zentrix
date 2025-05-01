package com.zentrix.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
public class Stock {
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long stockId;

    // Foreign Key
    @ManyToOne
    Branch brchId;

    @ManyToOne
    Supplier supplierId;

    @ManyToOne
    Staff createdBy;

    @OneToMany(mappedBy = "stockId", cascade = CascadeType.ALL, orphanRemoval = true)
    List<StockDetail> stockDetails;

    // Attribute
    LocalDate importDate;

    LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
