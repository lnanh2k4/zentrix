package com.zentrix.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.zentrix.model.entity.Image;
import com.zentrix.model.entity.ImagePost;
import com.zentrix.model.entity.Post;
import com.zentrix.repository.ImagePostRepository;
import com.zentrix.repository.ImageRepository;
import com.zentrix.repository.PostRepository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImagePostServiceImpl implements ImagePostService {

    final ImagePostRepository imagePostRepository;
    final ImageRepository imageRepository;
    final PostRepository postRepository;
    final FileService fileService;

    public ImagePostServiceImpl(ImagePostRepository imagePostRepository,
            ImageRepository imageRepository,
            PostRepository postRepository,
            FileService fileService) {
        this.imagePostRepository = imagePostRepository;
        this.imageRepository = imageRepository;
        this.postRepository = postRepository;
        this.fileService = fileService;
    }


    @Override
    public ImagePost uploadPostImage(Long postId, MultipartFile file) {
        String fileUrl = fileService.saveFile(file); 

        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            throw new RuntimeException("Post not found with ID: " + postId);
        }

        Image image = new Image();
        image.setImageLink(fileUrl);
        Image savedImage = imageRepository.save(image);

        ImagePost imagePost = new ImagePost();
        imagePost.setPost(postOpt.get());
        imagePost.setImage(savedImage);

        return imagePostRepository.save(imagePost);
    }


    @Override
    public List<ImagePost> getImagesByPostId(Long postId) {
        return imagePostRepository.findByPost_PostId(postId);
    }


    @Override
    public List<ImagePost> getPostsByImageId(Long imageId) {
        return imagePostRepository.findByImage_ImageId(imageId);
    }


    @Override
    public void deleteImagePost(Long imagePostId) {
        imagePostRepository.deleteById(imagePostId);
    }

}