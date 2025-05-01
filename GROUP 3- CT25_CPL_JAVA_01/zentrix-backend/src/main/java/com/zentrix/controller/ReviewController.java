package com.zentrix.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import com.zentrix.model.entity.ImageReview;
import com.zentrix.model.entity.Review;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.CheckConditionRequest;
import com.zentrix.model.request.ReviewRequest;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.repository.ImageReviewRepository;
import com.zentrix.repository.ReviewRepository;
import com.zentrix.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/*
* @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
* @date February 13, 2025
*/
@RestController
@RequestMapping("/api/v1/reviews")
@Slf4j
@Tag(name = "Review Controller", description = "Controller for managing reviews.")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ImageReviewRepository imageReviewRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * Retrieves all reviews.
     *
     * @param jwt The authorization token.
     * @return An ApiResponse containing a list of all reviews.
     */
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get all reviews", description = "Retrieve a list of all reviews.")
    @GetMapping("")
    public ResponseEntity<ResponseObject<List<Review>>> getAllReviews(@RequestParam(required = false) Long prodType) {
        List<Review> listReview;
        if (prodType != null) {
            listReview = reviewRepository.findByProduct_prodTypeId(prodType);
        } else {
            listReview = reviewRepository.findAll();
        }
        for (Review review : listReview) {
            List<ImageReview> imageReviews = imageReviewRepository.findByReview_ReviewId(review.getReviewId());
            if (!imageReviews.isEmpty()) {
                review.setImage(imageReviews.get(0).getImage().getImageLink());
            }
        }
        ResponseObject<List<Review>> response = new ResponseObject.Builder<List<Review>>()
                .content(listReview)
                .message("Get reviews list successfully!")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a review by its ID.
     *
     * @param id  The ID of the review to retrieve.
     * @param jwt The authorization token.
     * @return An ApiResponse containing the requested review.
     */
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get review by ID", description = "Retrieve details of a specific review using its ID.")
    @GetMapping("/{reviewId}")
    public ResponseEntity<ResponseObject<?>> getReviewById(@PathVariable Long reviewId
    // @RequestHeader("Authorization") String jwt
    ) {
        try {
            return ResponseEntity.ok(
                    new ResponseObject.Builder<Review>()
                            .content(reviewService.getReviewById(reviewId))
                            .message("Review retrieved successfully by ID.")
                            .code(200)
                            .success(true)
                            .build());
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject.Builder<String>()
                            .message(ex.getMessage())
                            .code(HttpStatus.NOT_FOUND.value())
                            .success(false)
                            .content(null)
                            .build());
        }
    }

    /**
     * Creates a new review submitted by a customer or admin.
     *
     * @param reviewRequest The review request containing product ID, user ID,
     *                      rating, comment, creation date, and optional image.
     * @param bindingResult The validation result from the @Valid annotation.
     * @return ResponseEntity with success message if creation is successful, or
     *         validation errors if input is invalid.
     */
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new review", description = "Add a new review to the system.")
    @PostMapping("/add")
    public ResponseEntity<ResponseObject<?>> createReview(
            @Valid @ModelAttribute ReviewRequest reviewRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(
                    new ResponseObject.Builder<Map<String, String>>()
                            .code(AppCode.FIELD_NOT_VALID.getCode())
                            .success(false)
                            .message("Validation failed")
                            .content(errors)
                            .build());
        }
        Map<String, String> Errors = new HashMap<>();
        if (reviewRequest.getRating() == null) {
            Errors.put("rating", AppCode.REVIEW_RATING_REQUIRED.getMessage());
        } else if (reviewRequest.getRating() < 1 || reviewRequest.getRating() > 5) {
            Errors.put("rating", AppCode.REVIEW_RATING_INVALID_RANGE.getMessage());
        }
        if (reviewRequest.getComment() == null ||
                reviewRequest.getComment().trim().isEmpty()) {
            Errors.put("comment", AppCode.REVIEW_COMMENT_REQUIRED.getMessage());
        }
        if (reviewRequest.getCreatedAt() == null) {
            reviewRequest.setCreatedAt(new java.sql.Date(System.currentTimeMillis()));
        } else if (reviewRequest.getCreatedAt().after(new Date())) {
            Errors.put("createdAt", AppCode.POST_CREATED_AT_IN_FUTURE.getMessage());
        }
        if (!Errors.isEmpty()) {
            log.warn("Manual validation failed with {} error(s): {}", Errors.size(),
                    Errors);
            return ResponseEntity.badRequest().body(
                    new ResponseObject.Builder<Map<String, String>>()
                            .code(AppCode.FIELD_NOT_VALID.getCode())
                            .success(false)
                            .message("Validation failed")
                            .content(Errors)
                            .build());
        }
        try {
            reviewService.createReview(reviewRequest, false);
            return ResponseEntity.ok(
                    new ResponseObject.Builder<Void>()
                            .code(200)
                            .success(true)
                            .message("Review created successfully")
                            .content(null)
                            .build());

        } catch (HttpStatusCodeException ex) {
            System.out.println("Received data: productId=" + reviewRequest.getProductId() + ", userId="
                    + reviewRequest.getUserId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ResponseObject.Builder<String>()
                            .code(AppCode.FIELD_NOT_VALID.getCode())
                            .success(false)
                            .message(ex.getMessage())
                            .content(null)
                            .build());
        }
    }

    /**
     * Deletes a review by its ID.
     *
     * @param id  The ID of the review to delete.
     * @param jwt The authorization token.
     * @return An ApiResponse indicating the success of the delete operation.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER_STAFF','CUSTOMER','WAREHOUSE STAFF','SELLER STAFF')")
    @Operation(summary = "Delete a review", description = "Remove a review from the system using its ID.")
    @DeleteMapping("/remove/{reviewId}")
    public ResponseEntity<ResponseObject<?>> deleteReview(@PathVariable Long reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok(
                    new ResponseObject.Builder<Void>()
                            .code(200)
                            .success(true)
                            .message("Review delete successfully")
                            .content(null)
                            .build());
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ResponseObject.Builder<String>()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .message(ex.getMessage())
                            .content(null)
                            .build());
        }
    }

    /**
     * API to check if a user is eligible to review a product.
     *
     * @param productId the ID of the product
     * @param userId    the ID of the user
     * @return response indicating whether the review condition check was successful
     */

    @PostMapping("/check-condition")
    public ResponseEntity<ResponseObject<?>> checkConditonReview(
            @RequestParam Long productId,
            @RequestParam Long userId) {
        try {
            CheckConditionRequest reviewRequest = new CheckConditionRequest(productId, userId);
            reviewService.checkConditionReview(reviewRequest);
            return ResponseEntity.ok(
                    new ResponseObject.Builder<Void>()
                            .code(200)
                            .success(true)
                            .message("Review condition checked successfully")
                            .content(null)
                            .build());

        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ResponseObject.Builder<String>()
                            .code(AppCode.FIELD_NOT_VALID.getCode())
                            .success(false)
                            .message(ex.getMessage())
                            .content(null)
                            .build());
        }
    }

    /**
     * Returns public reviews for a given product.
     *
     * @param productId the ID of the product
     * @return response with list of reviews
     */
    @GetMapping("/public/{productId}")
    public ResponseEntity<ResponseObject<List<Review>>> getPublicReviews(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getPublicReviewsByProductId(productId);

        ResponseObject<List<Review>> response = new ResponseObject.Builder<List<Review>>()
                .content(reviews)
                .code(200)
                .message("Get reviews successfully")
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }

}
