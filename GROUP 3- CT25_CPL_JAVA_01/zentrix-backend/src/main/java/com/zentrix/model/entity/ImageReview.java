package com.zentrix.model.entity;

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
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ImageReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long imagRevwId;

    @ManyToOne
    @JoinColumn(name = "review_id", referencedColumnName = "reviewId", nullable = false)
    Review review;

    @ManyToOne
    @JoinColumn(name = "image_id", referencedColumnName = "imageId", nullable = false)
    Image image;

}
