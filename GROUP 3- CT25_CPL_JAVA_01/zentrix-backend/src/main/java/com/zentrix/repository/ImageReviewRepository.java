package com.zentrix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zentrix.model.entity.ImageReview;
import com.zentrix.model.entity.Review;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */
public interface ImageReviewRepository extends JpaRepository<ImageReview, Long> {
    List<ImageReview> findByReview_ReviewId(Long reviewId);

    List<ImageReview> findByImage_ImageId(Long imageId);

    void deleteByReview(Review review);
}
