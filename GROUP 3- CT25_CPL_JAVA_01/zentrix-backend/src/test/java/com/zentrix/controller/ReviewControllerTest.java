package com.zentrix.controller;


import com.zentrix.model.entity.Review;
import com.zentrix.model.request.ReviewRequest;
import com.zentrix.repository.ImageReviewRepository;
import com.zentrix.repository.ReviewRepository;
import com.zentrix.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.sql.Date;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date April 07, 2025
 */
@ExtendWith(MockitoExtension.class)
public class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @Mock
    private ImageReviewRepository imageReviewRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewController reviewController;

    private ReviewRequest reviewRequest;
    private Review review;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();

        reviewRequest = new ReviewRequest();
        reviewRequest.setComment("Great product!");
        reviewRequest.setRating(5);
        reviewRequest.setProductId(1L);
        reviewRequest.setUserId(100L);
        reviewRequest.setCreatedAt(new Date(System.currentTimeMillis()));

        review = Review.builder()
                .reviewId(1L)
                .comment("Great product!")
                .rating(5)
                .createdAt(new Date(System.currentTimeMillis()))
                .build();
    }

   

    @Test
    void testGetReviewById_Success() throws Exception {
        when(reviewService.getReviewById(1L)).thenReturn(review);

        mockMvc.perform(get("/api/v1/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Review retrieved successfully by ID."))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.content.comment").value("Great product!"));

        verify(reviewService, times(1)).getReviewById(1L);
    }

  
 

    @Test
    void testDeleteReview_Success() throws Exception {
        doNothing().when(reviewService).deleteReview(1L);

        mockMvc.perform(delete("/api/v1/reviews/remove/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Review delete successfully"))
                .andExpect(jsonPath("$.code").value(200));

        verify(reviewService, times(1)).deleteReview(1L);
    }

  
}