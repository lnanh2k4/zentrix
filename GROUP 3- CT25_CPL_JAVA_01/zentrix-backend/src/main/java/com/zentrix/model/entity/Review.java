package com.zentrix.model.entity;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 11, 2025
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Primary key
    Long reviewId;
    // Foreign key
    @ManyToOne
    @JoinColumn(name = "prodTypeId", nullable = false)
    ProductType product;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;
    // Normal key
    @Column(nullable = false, columnDefinition = "TEXT")
    String comment;
    @Column(nullable = false)
    Integer rating;
    @Column(nullable = false)
    Date createdAt;
    private String image;
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
