package com.zentrix.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.zentrix.model.entity.Image;
import com.zentrix.model.entity.ImageReview;
import com.zentrix.model.entity.Review;
import com.zentrix.repository.ImageRepository;
import com.zentrix.repository.ImageReviewRepository;
import com.zentrix.repository.ReviewRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageReviewServiceImpl implements ImageReviewService {
    final ImageReviewRepository imageReviewRepository;
    final ImageRepository imageRepository;
    final ReviewRepository reviewRepository;
    final FileService fileService;


    public ImageReviewServiceImpl(ImageReviewRepository imageReviewRepository,
            ImageRepository imageRepository,
            ReviewRepository reviewRepository,
            FileService fileService) {
        this.imageReviewRepository = imageReviewRepository;
        this.imageRepository = imageRepository;
        this.reviewRepository = reviewRepository;
        this.fileService = fileService;
    }


    @Override
    public ImageReview uploadReviewImage(Long reviewId, MultipartFile file) {
        String fileUrl = fileService.saveFile(file); 

        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            throw new RuntimeException("Review not found with ID: " + reviewId);
        }

        Image image = new Image();
        image.setImageLink(fileUrl);
        Image savedImage = imageRepository.save(image);

        ImageReview imageReview = new ImageReview();
        imageReview.setReview(reviewOpt.get());
        imageReview.setImage(savedImage);

        return imageReviewRepository.save(imageReview);
    }


    @Override
    public List<ImageReview> getImagesByReviewId(Long reviewId) {
        return imageReviewRepository.findByReview_ReviewId(reviewId);
    }


    @Override
    public List<ImageReview> getReviewsByImageId(Long imageId) {
        return imageReviewRepository.findByImage_ImageId(imageId);
    }


    @Override
    public void deleteImageReview(Long imageReviewId) {
        reviewRepository.deleteById(imageReviewId);
    }
}
