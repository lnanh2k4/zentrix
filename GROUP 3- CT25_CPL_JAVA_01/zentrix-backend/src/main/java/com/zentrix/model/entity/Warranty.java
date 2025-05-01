package com.zentrix.model.entity;

import java.time.LocalDateTime;
import java.util.Date;

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
* @author Dang Cong Khanh - CE180117 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/

@Entity
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class Warranty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long warnId;

    // Forgening key
    @ManyToOne
    User userId;
    @ManyToOne
    ProductType prodTypeId;
    @ManyToOne
    Staff createdBy;

    // Attribute
    @Column(nullable = false)
    Date warnStartDate;
    @Column(nullable = false)
    Date warnEndDate;
    @Column(length = 255, nullable = false)
    String description;
    @Column(length = 255, nullable = false)
    String receive;
    @Column(nullable = false)
    Integer status;
}
