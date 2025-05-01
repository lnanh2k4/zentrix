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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

import com.zentrix.model.entity.ImageReview;
import com.zentrix.model.entity.Review;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.ReviewRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.repository.ImageReviewRepository;
import com.zentrix.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date March 28, 2025
 */
@RestController
@RequestMapping("/api/v1/dashboard/reviews")
@Slf4j
public class ReviewDashboardController {
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ImageReviewRepository imageReviewRepository;

    /**
     * Retrieves all reviews.
     *
     * @param jwt The authorization token.
     * @return An ApiResponse containing a list of all reviews.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all reviews", description = "Retrieve a list of all reviews.")
    @GetMapping("")
    public ResponseEntity<ResponseObject<List<Review>>> getAllReviews(
            // @RequestHeader("Authorization") String jwt
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginationWrapper<List<Review>> wrapper = reviewService.getAllReviews(page,
                size);
        for (Review review : wrapper.getData()) {
            List<ImageReview> imageReviews = imageReviewRepository.findByReview_ReviewId(review.getReviewId());
            if (!imageReviews.isEmpty()) {
                review.setImage(imageReviews.get(0).getImage().getImageLink());
            }
        }
        ResponseObject<List<Review>> response = new ResponseObject.Builder<List<Review>>()
                .unwrapPaginationWrapper(wrapper)
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
    @PreAuthorize("hasRole('ADMIN')")
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
     * Creates a new review.
     *
     * @param reviewRequest The request object containing review details.
     * @param jwt           The authorization token.
     * @return An ApiResponse containing the newly created review.
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
            System.out.println("Received data: productId=" + reviewRequest.getProductId() + ", userId=" + reviewRequest.getUserId());
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
     * Updates an existing review.
     *
     * @param id            The ID of the review to update.
     * @param reviewRequest The request object containing updated review details.
     * @param jwt           The authorization token.
     * @return An ApiResponse containing the updated review.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER_STAFF')")
    @Operation(summary = "Update a review", description = "Modify the details of an existing review by its ID.")
    @PutMapping("/update/{reviewId}")
    public ResponseEntity<ResponseObject<?>> updateReview(
            @PathVariable Long reviewId,
            @Valid @ModelAttribute ReviewRequest reviewRequest,
            BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(e -> {
                errors.put(e.getField(), e.getDefaultMessage());
                log.error("Validation error - {}: {}", e.getField(), e.getDefaultMessage());
            });
        }
        if (reviewRequest.getRating() == null) {
            errors.put("rating", AppCode.REVIEW_RATING_REQUIRED.getMessage());
        } else if (reviewRequest.getRating() < 1 || reviewRequest.getRating() > 5) {
            errors.put("rating", AppCode.REVIEW_RATING_INVALID_RANGE.getMessage());
        }
        if (reviewRequest.getComment() != null &&
                reviewRequest.getComment().trim().isEmpty()) {
            errors.put("comment", AppCode.REVIEW_COMMENT_REQUIRED.getMessage());
        }
        if (reviewRequest.getCreatedAt() == null) {
            reviewRequest.setCreatedAt(new java.sql.Date(System.currentTimeMillis()));
        } else if (reviewRequest.getCreatedAt().after(new Date())) {
            errors.put("createdAt", AppCode.REVIEW_CREATED_AT_IN_FUTURE.getMessage());
        }

        if (!errors.isEmpty()) {
            log.warn("Update review validation failed: {}", errors);
            return ResponseEntity.badRequest().body(
                    new ResponseObject.Builder<Map<String, String>>()
                            .code(AppCode.FIELD_NOT_VALID.getCode())
                            .success(false)
                            .message("Validation failed")
                            .content(errors)
                            .build());
        }
        try {
            Review review = reviewService.updateReview(reviewId, reviewRequest);
            return ResponseEntity.ok(
                    new ResponseObject.Builder<Review>()
                            .code(200)
                            .success(true)
                            .message("Review updated successfully")
                            .content(review)
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

    
}
