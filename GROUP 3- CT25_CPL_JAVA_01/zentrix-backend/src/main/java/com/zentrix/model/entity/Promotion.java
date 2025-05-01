package com.zentrix.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promId;

    // Forgening key
    @ManyToOne
    Staff createdBy;

    @ManyToOne
    Staff approvedBy;

    // Attribute
    @Column(length = 255, nullable = false)
    String promName;
    @Column(length = 255, nullable = false)
    String promCode;
    @Column(nullable = false)
    Float discount;
    @Column(nullable = false)
    Date startDate;
    @Column(nullable = false)
    Date endDate;
    @Column(nullable = false)
    Integer quantity;
    @Column(nullable = false)
    Integer promStatus;

    @OneToMany(mappedBy = "promId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<UserPromotion> userPromotions;

    @OneToMany(mappedBy = "promId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<Order> orders;
}
