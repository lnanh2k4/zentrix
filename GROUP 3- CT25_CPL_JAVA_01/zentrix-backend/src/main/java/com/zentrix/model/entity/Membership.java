package com.zentrix.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Membership {
    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long mbsId;
    // Attribute
    @Column(nullable = false)
    String mbsName;
    @Column(nullable = false)
    Long mbsPoint;

    String mbsDescription;

}
