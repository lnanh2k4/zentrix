package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.Review;
import com.zentrix.model.request.CheckConditionRequest;
import com.zentrix.model.request.ReviewRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
* @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
* @date February 13, 2025
*/

public interface ReviewService {

/**
* Retrieves all reviews.
*
* @return A list of all reviews in the system.
*/
PaginationWrapper <List<Review>> getAllReviews(int page, int size);

/**
* Retrieves a specific review by its ID.
*
* @param reviewId The ID of the review to retrieve.
* @return The Review object if found.
* @throws AppException if the review is not found.
*/
Review getReviewById(Long reviewId);

/**
* Creates a new review based on the provided request data.
*
* @param reviewRequest The request object containing review details.
* @return The newly created Review object.
* @throws AppException if there is an issue with saving the review.
*/
void createReview(ReviewRequest reviewRequest, boolean isAdddDatabaseInit);

/**
* Updates an existing review with new data.
*
* @param reviewId The ID of the review to update.
* @param reviewRequest The request object containing updated review details.
* @return The updated Review object.
* @throws AppException if the review is not found or the update fails.
*/
Review updateReview(Long reviewId, ReviewRequest reviewRequest);

/**
* Deletes a review by its ID.
*
* @param reviewId The ID of the review to delete.
* @throws AppException if the review does not exist or cannot be deleted.
*/
void deleteReview(Long reviewId);
/**
 * Checks if the user is eligible to submit a review.
 *
 * @param reviewRequest the request containing review-related info
 * @return true if conditions are met; false otherwise
 */
boolean checkConditionReview(CheckConditionRequest reviewRequest);

public List<Review> getPublicReviewsByProductId(Long productId) ;
}
