package com.zentrix.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.zentrix.model.entity.ImageReview;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */
public interface ImageReviewService {

    /**
     * Uploads an image for a specific review.
     *
     * @param reviewId The ID of the review to associate with the image.
     * @param file     The image file to be uploaded.
     * @return The saved ImageReview entity.
     */
    ImageReview uploadReviewImage(Long reviewId, MultipartFile file);

    /**
     * Retrieves all images associated with a given review.
     *
     * @param reviewId The ID of the review.
     * @return A list of ImageReview entities linked to the review.
     */
    List<ImageReview> getImagesByReviewId(Long reviewId);

    /**
     * Retrieves all reviews associated with a given image.
     *
     * @param imageId The ID of the image.
     * @return A list of ImageReview entities linked to the image.
     */
    List<ImageReview> getReviewsByImageId(Long imageId);

    /**
     * Deletes an ImageReview entry from the database.
     *
     * @param imageReviewId The ID of the ImageReview entry to delete.
     */
    void deleteImageReview(Long imageReviewId);
}
