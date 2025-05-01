package com.zentrix.service;

import com.zentrix.model.entity.*;
import com.zentrix.model.request.ReviewRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;

import java.sql.Date;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductTypeRepository productTypeRepository;
    @Mock private UserRepository userRepository;
    @Mock private ImageRepository imageRepository;
    @Mock private ImageReviewRepository imageReviewRepository;
    @Mock private FileServiceImpl fileServiceImpl;
    @Mock private OrderDetailRepository orderDetailRepository;
    @Mock private OrderRepository orderRepository;

    private ReviewRequest reviewRequest;
    private ProductType productType;
    private ProductTypeBranch productTypeBranch;
    private User user;
    private Review review;
    private Image image;
    private MockMultipartFile imageFile;

    @BeforeEach
    void init() {
        imageFile = new MockMultipartFile("image.jpg", new byte[1]);

        productTypeBranch = new ProductTypeBranch();
        productTypeBranch.setProdTypeBrchId(1L);

        productType = new ProductType();
        productType.setProdTypeId(1L);
        productType.setProductTypeBranches(new ArrayList<>(Set.of(productTypeBranch)));

        user = new User();
        user.setUserId(1L);
        user.setUsername("test_user");

        reviewRequest = new ReviewRequest();
        reviewRequest.setProductId(1L);
        reviewRequest.setUserId(1L);
        reviewRequest.setComment("Nice!");
        reviewRequest.setRating(5);
        reviewRequest.setImageFile(imageFile);

        review = Review.builder()
                .reviewId(1L)
                .product(productType)
                .user(user)
                .comment("Nice!")
                .rating(5)
                .createdAt(new Date(System.currentTimeMillis()))
                .build();

        image = Image.builder().imageId(1L).imageLink("https://cdn.test.jpg").build();
    }

    @Test
    void testGetReviewById_success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        Review found = reviewService.getReviewById(1L);
        assertEquals("Nice!", found.getComment());
    }

    @Test
    void testGetAllReviews_success() {
        Page<Review> reviewPage = new PageImpl<>(List.of(review));
        when(reviewRepository.findAll(any(Pageable.class))).thenReturn(reviewPage);
        PaginationWrapper<List<Review>> result = reviewService.getAllReviews(0, 10);
        assertEquals(1, result.getData().size());
    }

    @Test
    void testUpdateReview_success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any())).thenReturn(review);
        when(fileServiceImpl.saveFile(imageFile)).thenReturn("https://cdn.test.jpg");
        when(imageRepository.save(any(Image.class))).thenReturn(image);

        reviewRequest.setComment("Updated!");
        reviewRequest.setRating(4);
        reviewRequest.setImageFile(imageFile);

        Review result = reviewService.updateReview(1L, reviewRequest);
        assertEquals("Updated!", result.getComment());
        assertEquals(4, result.getRating());
    }

    @Test
    void testDeleteReview_success() {
        ImageReview imageReview = ImageReview.builder()
                .review(review)
                .image(image)
                .build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(imageReviewRepository.findByReview_ReviewId(1L)).thenReturn(List.of(imageReview));
        when(imageReviewRepository.findByImage_ImageId(1L)).thenReturn(Collections.emptyList());

        reviewService.deleteReview(1L);
        verify(reviewRepository).delete(review);
        verify(imageRepository).deleteById(1L);
    }
}
