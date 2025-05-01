package com.zentrix.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.zentrix.model.entity.Image;
import com.zentrix.model.entity.ImagePost;
import com.zentrix.model.entity.ImageReview;
import com.zentrix.model.entity.Post;
import com.zentrix.model.entity.Review;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.repository.ImagePostRepository;
import com.zentrix.repository.ImageRepository;
import com.zentrix.repository.ImageReviewRepository;
import com.zentrix.repository.PostRepository;
import com.zentrix.repository.ReviewRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileServiceImpl implements FileService {

    PostRepository postRepository;
    ImageRepository imageRepository;
    ImagePostRepository imagePostRepository;
    ReviewRepository reviewRepository;
    ImageReviewRepository imageReviewRepository;


    final String uploadPath = System.getProperty("user.dir") + File.separator + "uploads"; 
    final String pathPrefix = "http://localhost:6789/uploads/";
    @Override
    public String saveFile(MultipartFile file) {
        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String baseName = originalFileName.substring(0, originalFileName.lastIndexOf("."));

            String uniqueID = UUID.randomUUID().toString();
            String fileName = baseName + "_" + uniqueID + fileExtension;

            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String filePath = uploadPath + File.separator + fileName;
            file.transferTo(new File(filePath));

            log.info("File saved at: {}", filePath);
            return pathPrefix + fileName;

        } catch (IOException e) {
            log.error("File save error: {}", e.getMessage());
            throw new ValidationFailedException(AppCode.FILE_UPLOAD_ERROR);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String filePath = fileUrl.replace("http://localhost:6789/uploads/", uploadPath + File.separator);
            Path path = Paths.get(filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                System.out.println("file deleted successfully:" + filePath);
            } else {
                System.out.println("file not found: " + filePath);
            }
        } catch (Exception e) {
            System.out.println("Failed to delete file" + fileUrl);
            e.printStackTrace();
        }
    }

    public void updateUrlImageForDatabaseInit(long id ,String url){
        try { 
            var image = new Image();
            image.setImageLink(url);
            var imageObject = imageRepository.save(image);
            Post currentPost = postRepository.findById(id).orElse(null);
            var imagePost = new ImagePost();
            imagePost.setImage(imageObject);
            imagePost.setPost(currentPost);
            imageRepository.save(image); 
            imagePostRepository.save(imagePost);
        } catch(Exception e) {

        }
    }

    public void updateUrlImageReviewForDatabaseInit(long id, String url){
        try {
            var image = new Image();
            image.setImageLink(url);
            var imageObject = imageRepository.save(image);
            Review currentReview = reviewRepository.findById(id).orElse(null);
            var imageReview = new ImageReview();
            imageReview.setImage(imageObject);
            imageReview.setReview(currentReview);
            imageRepository.save(image);
            imageReviewRepository.save(imageReview);
        } catch (Exception e) {
        }
    }

}
