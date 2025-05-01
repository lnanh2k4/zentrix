package com.zentrix.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.zentrix.model.entity.Image;
import com.zentrix.model.entity.ImageReview;
import com.zentrix.model.entity.Order;
import com.zentrix.model.entity.OrderDetail;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.ProductTypeBranch;
import com.zentrix.model.entity.Review;
import com.zentrix.model.entity.User;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.model.request.CheckConditionRequest;
import com.zentrix.model.request.ReviewRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.ImageRepository;
import com.zentrix.repository.ImageReviewRepository;
import com.zentrix.repository.OrderDetailRepository;
import com.zentrix.repository.OrderRepository;
import com.zentrix.repository.ProductTypeRepository;
import com.zentrix.repository.ReviewRepository;
import com.zentrix.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import java.sql.Date;
/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date April 09, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewServiceImpl implements ReviewService {

    ReviewRepository reviewRepository;
    ProductTypeRepository productTypeRepository;
    ImageRepository imageRepository;
    UserRepository userRepository;
    ImageReviewRepository imageReviewRepository;
    FileServiceImpl fileServiceImpl;
    OrderDetailRepository orderDetailRepository;
    OrderRepository orderRepository;

    @Override
    public PaginationWrapper<List<Review>> getAllReviews(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reviewId"));
            Page<Review> reviewPage = reviewRepository.findAll(pageable);
            return new PaginationWrapper.Builder<List<Review>>()
                    .setData(reviewPage.getContent())
                    .setPaginationInfo(reviewPage)
                    .build();
        } catch (Exception e) {
            throw new ActionFailedException(AppCode.REVIEW_INTERNAL_ERROR);
        }
    }

    @Override
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId).orElse(null);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void createReview(ReviewRequest reviewRequest, boolean isAdddDatabaseInit) {
        if (reviewRequest.getProductId() == null || reviewRequest.getUserId() == null) {
            throw new ValidationFailedException(AppCode.INVALID_INPUT);
        }

        ProductType product = productTypeRepository.findById(reviewRequest.getProductId())
                .orElseThrow(() -> new ValidationFailedException(AppCode.PRODUCT_NOT_FOUND));

        User user = userRepository.findById(reviewRequest.getUserId())
                .orElseThrow(() -> new ValidationFailedException(AppCode.USER_NOT_FOUND));

        if (!isAdddDatabaseInit) {
            boolean isBought = false;
            List<OrderDetail> listOdt = orderDetailRepository.findByUserId(user.getUserId());
            Integer boughtTime = 0;
            ProductTypeBranch prodTp = null;
            for (OrderDetail od : listOdt) {
                if (product.getProductTypeBranches().contains(od.getProdTypeBranchId())
                        && od.getOrderId().getStatus() == 5) {
                    prodTp = od.getProdTypeBranchId();
                    isBought = true;
                    boughtTime += 1;
                }
            }
            final ProductTypeBranch finalProdTp = prodTp;

            List<Review> reviews = reviewRepository.findByUser(user);
            int reviewTime = (int) reviews.stream()
                    .filter(review -> review.getProduct().getProductTypeBranches().contains(finalProdTp))
                    .count();
            boolean isHasLeftReview = (boughtTime - reviewTime) > 0;

            if (!isBought || !isHasLeftReview)
                throw new ValidationFailedException(AppCode.REVIEW_NOT_FOUND);
        }
        Review review = Review.builder()
                .product(product)
                .user(user)
                .comment(reviewRequest.getComment())
                .rating(reviewRequest.getRating())
                .createdAt(reviewRequest.getCreatedAt() != null ? reviewRequest.getCreatedAt()
                        : new Date(System.currentTimeMillis()))
                .build();
        review = reviewRepository.save(review);
        log.info("product" + reviewRequest.getProductId());
        log.info("User ne " + reviewRequest.getUserId());

        if (reviewRequest.getImageFile() != null) {
            MultipartFile imageFile = reviewRequest.getImageFile();
        log.info("📦 File received: name={}, size={} KB, contentType={}-----------------++++++++++++++++____________________________+++++++++++++++++++++++",
        imageFile.getOriginalFilename(),
        imageFile.getSize() / 1024,
        imageFile.getContentType());

            String urlImage = fileServiceImpl.saveFile(reviewRequest.getImageFile());
            if (urlImage == null) {
                throw new ActionFailedException(AppCode.FILE_UPLOAD_ERROR);
            }

            Image image = Image.builder().imageLink(urlImage).build();
            image = imageRepository.save(image);
            if (image.getImageId() == null)
                throw new ActionFailedException(AppCode.REVIEW_CREATION_FAILED);
            ImageReview imageReview = ImageReview.builder().review(review).image(image).build();
            imageReviewRepository.save(imageReview);
        }
        if (review.getReviewId() == null) {
            throw new ActionFailedException(AppCode.REVIEW_CREATION_FAILED);
        }

    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Review updateReview(Long reviewId, ReviewRequest reviewRequest) {
        return reviewRepository.findById(reviewId).map(existingReview -> {
            existingReview.setComment(reviewRequest.getComment());
            existingReview.setRating(reviewRequest.getRating());

            if (reviewRequest.getImageFile() != null && !reviewRequest.getImageFile().isEmpty()) {
                List<ImageReview> imageReviews = imageReviewRepository.findByReview_ReviewId(reviewId);
                for (ImageReview imageReview : imageReviews) {
                    fileServiceImpl.deleteFile(imageReview.getImage().getImageLink());
                }
                imageReviewRepository.deleteByReview(existingReview);

                String newImageUrl = fileServiceImpl.saveFile(reviewRequest.getImageFile());
                Image newImage = Image.builder().imageLink(newImageUrl).build();
                Image savedImage = imageRepository.save(newImage);
                ImageReview newImageReview = ImageReview.builder().review(existingReview).image(savedImage).build();
                imageReviewRepository.save(newImageReview);
            }
            return reviewRepository.save(existingReview);
        }).orElseThrow(() -> new ValidationFailedException(AppCode.REVIEW_NOT_FOUND));
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void deleteReview(Long reviewId) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ValidationFailedException(AppCode.REVIEW_NOT_FOUND));

        List<ImageReview> imageReviews = imageReviewRepository.findByReview_ReviewId(reviewId);
        for (ImageReview imageReview : imageReviews) {
            fileServiceImpl.deleteFile(imageReview.getImage().getImageLink());
        }

        imageReviewRepository.deleteByReview(existingReview);

        List<Long> imageIds = imageReviews.stream()
                .map(imageReview -> imageReview.getImage().getImageId())
                .distinct()
                .collect(Collectors.toList());

        for (Long imageId : imageIds) {
            if (imageReviewRepository.findByImage_ImageId(imageId).isEmpty()) {
                imageRepository.deleteById(imageId);
            }
        }

        reviewRepository.delete(existingReview);
    }

    @Override
    public boolean checkConditionReview(CheckConditionRequest reviewRequest) {
        System.out.println(reviewRequest.getProductId().toString());
        ProductType product = productTypeRepository.findById(reviewRequest.getProductId())
        .orElseThrow(() -> new ValidationFailedException(AppCode.PRODUCT_NOT_FOUND));

        User user = userRepository.findById(reviewRequest.getUserId())
        .orElseThrow(() -> new ValidationFailedException(AppCode.USER_NOT_FOUND));

        List<OrderDetail> listOdt = orderDetailRepository.findByUserId(user.getUserId());
        Integer boughtTime = 0;
        ProductTypeBranch prodTp = null;
        for (OrderDetail od : listOdt) {
            if (product.getProductTypeBranches().contains(od.getProdTypeBranchId())
                    && od.getOrderId().getStatus() == 5) {
                prodTp = od.getProdTypeBranchId();
                boughtTime += 1;
            }
        }
        final ProductTypeBranch finalProdTp = prodTp;

        List<Review> reviews = reviewRepository.findByUser(user);
        int reviewTime = (int) reviews.stream()
                .filter(review -> review.getProduct().getProductTypeBranches().contains(finalProdTp))
                .count();
        boolean isHasLeftReview = (boughtTime - reviewTime) > 0;

        if ( !isHasLeftReview){
            throw new ValidationFailedException(AppCode.REVIEW_NOT_FOUND);
        }
        return true;
}

@Override
public List<Review> getPublicReviewsByProductId(Long productId) {
    List<Review> reviews = reviewRepository.findAllByProductIdAndActiveUser(productId);

    for (Review review : reviews) {
        List<ImageReview> imageReviews = imageReviewRepository.findByReview_ReviewId(review.getReviewId());
        if (!imageReviews.isEmpty()) {
            review.setImage(imageReviews.get(0).getImage().getImageLink()); 
        }
    }
    return reviews;
}

}
