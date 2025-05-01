package com.zentrix.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.zentrix.model.entity.Image;
import com.zentrix.model.entity.ImagePost;
import com.zentrix.model.entity.Post;
import com.zentrix.model.entity.Staff;
import com.zentrix.model.entity.User;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.model.request.PostRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.utils.Status;
import com.zentrix.repository.ImagePostRepository;
import com.zentrix.repository.ImageRepository;
import com.zentrix.repository.PostRepository;
import com.zentrix.repository.RoleRepository;
import com.zentrix.repository.StaffRepository;
import com.zentrix.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date March 14, 2025
 */

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostServiceImpl implements PostService {
    PostRepository postRepository;
    FileServiceImpl fileServiceImpl;
    StaffRepository staffRepository;
    ImageRepository imageRepository;
    ImagePostRepository imagePostRepository;
    UserRepository userRepository;
    RoleRepository roleRepository;
    @Override
    public PaginationWrapper<List<Post>> getAllPosts(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postId"));
            Page<Post> postPage = postRepository.findAll(pageable);
            List<Post> posts = postPage.getContent();
            if (posts.isEmpty() && page < postPage.getTotalPages()) {
                log.warn("No posts found for page {} with size {}", page, size);
            }

            if (posts.size() < size && page == postPage.getTotalPages() - 1) {
                log.info("Returning all remaining posts for last page {}", page);
            }

            return new PaginationWrapper.Builder<List<Post>>()
                    .setData(posts)
                    .setPaginationInfo(postPage)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching posts with pagination: ", e);
            throw new ActionFailedException(AppCode.POST_INTERNAL_ERROR);
        }
    }

    @Transactional
    @Override
    public Post getPostById(Long postId) {
        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty())
            return null;
        Post currentPost = post.get();
        List<ImagePost> imagePosts = imagePostRepository.findByPost_PostId(currentPost.getPostId());
        List<String> imageLinks = imagePosts.stream()
                .map(imagePost -> imagePost.getImage().getImageLink())
                .toList();
        currentPost.setImages(imageLinks);
        return currentPost;
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void createPost(@Valid PostRequest postRequest) {
        log.info("createdById: {}", postRequest.getCreatedBy());

        if (postRequest.getCreatedBy() == null) {
            throw new ValidationFailedException(AppCode.INVALID_INPUT);
        }

        // Nguoi tao bai viet -> Seller_Staff or Admin
        User createdBy = userRepository.findById(postRequest.getCreatedBy())
                .orElseThrow(() -> new ValidationFailedException(AppCode.USER_NOT_FOUND));
        boolean isAdminOrStaff = createdBy.getRoleId().getRoleName().toString() != com.zentrix.model.utils.Role.ADMIN.toString()
                || createdBy.getRoleId().getRoleName().toString() != com.zentrix.model.utils.Role.SELLER_STAFF.toString();
        if(!isAdminOrStaff) {
            throw new ValidationFailedException(AppCode.USER_NOT_AUTHORIZED);
        }
        Staff staff =  staffRepository.findStaffByUserId(createdBy);
        if (staff == null) {
            throw new ValidationFailedException(AppCode.USER_NOT_AUTHORIZED);
        }
        Status finalStatus = postRequest.getStatus() != null ? postRequest.getStatus() : Status.VERIFYING;

        Post post = Post.builder()
                .title(postRequest.getTitle())
                .description(postRequest.getDescription())
                .createdBy(staff)
                .createdAt(postRequest.getCreatedAt())
                .status(finalStatus)
                .build();
        post = postRepository.save(post);

        if (postRequest.getImageFiles() != null && postRequest.getImageFiles().length > 0) {
            for (MultipartFile file : postRequest.getImageFiles()) {
                String urlImage = fileServiceImpl.saveFile(file);
                if (urlImage == null) {
                    throw new ActionFailedException(AppCode.FILE_UPLOAD_ERROR);
                }

                Image image = Image.builder().imageLink(urlImage).build();
                image = imageRepository.save(image);

                if (image.getImageId() == null) {
                    throw new ActionFailedException(AppCode.POST_CREATION_FAILED);
                }

                ImagePost imagePost = ImagePost.builder().post(post).image(image).build();
                imagePostRepository.save(imagePost);
            }
        }
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Post updatePost(Long postId, PostRequest postRequest) {
        return postRepository.findById(postId).map(existingPost -> {

            existingPost.setTitle(postRequest.getTitle());
            existingPost.setDescription(postRequest.getDescription());

            if (postRequest.getCreatedAt() != null) {
                existingPost.setCreatedAt(postRequest.getCreatedAt());
            }

            List<String> keptImages = postRequest.getExistingImageLinks() != null
                    ? List.of(postRequest.getExistingImageLinks())
                    : List.of();

            List<ImagePost> existingImagePosts = imagePostRepository.findByPost_PostId(postId);

            for (ImagePost imagePost : existingImagePosts) {
                String imageUrl = imagePost.getImage().getImageLink();
                if (!keptImages.contains(imageUrl)) {
                    fileServiceImpl.deleteFile(imageUrl);
                    imagePostRepository.delete(imagePost);

                    if (imagePostRepository.findByImage_ImageId(imagePost.getImage().getImageId()).isEmpty()) {
                        imageRepository.deleteById(imagePost.getImage().getImageId());
                    }
                }
            }

            if (postRequest.getImageFiles() != null && postRequest.getImageFiles().length > 0) {
                for (MultipartFile file : postRequest.getImageFiles()) {
                    String newImageUrl = fileServiceImpl.saveFile(file);
                    Image newImage = Image.builder().imageLink(newImageUrl).build();
                    Image savedImage = imageRepository.save(newImage);

                    ImagePost newImagePost = ImagePost.builder()
                            .post(existingPost)
                            .image(savedImage)
                            .build();
                    imagePostRepository.save(newImagePost);
                }
            }
            log.info("Description to save: {}", existingPost.getDescription());
            log.info("Description from request: {}", postRequest.getDescription());
            postRepository.flush();
            return postRepository.save(existingPost);

        }).orElseThrow(() -> new ValidationFailedException(AppCode.POST_NOT_FOUND));
    }

    @Transactional
    @Override
    public void deletePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ValidationFailedException(AppCode.POST_NOT_FOUND);
        }

        List<ImagePost> imagePosts = imagePostRepository.findByPost_PostId(postId);
        for (ImagePost imagePost : imagePosts) {
            String imageUrl = imagePost.getImage().getImageLink();
            fileServiceImpl.deleteFile(imageUrl);
        }

        List<Long> imageIds = imagePosts.stream()
                .map(imagePost -> imagePost.getImage().getImageId())
                .distinct()
                .collect(Collectors.toList());

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ValidationFailedException(AppCode.POST_NOT_FOUND));

        imagePostRepository.deleteByPost(post);

        for (Long imageId : imageIds) {
            if (imagePostRepository.findByImage_ImageId(imageId).isEmpty()) {
                imageRepository.deleteById(imageId);
            }
        }

        postRepository.deleteById(postId);
    }

    @Transactional
    @Override
    public void approvePost(Long postId, Long approvedById) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ValidationFailedException(AppCode.POST_NOT_FOUND));
                User user = userRepository.findById(approvedById)
                .orElseThrow(() -> new ValidationFailedException(AppCode.USER_NOT_FOUND));
        Staff approvedBy = staffRepository.findStaffByUserId(user);
        if (approvedBy == null) {
            throw new ValidationFailedException(AppCode.STAFF_NOT_FOUND);
        }

        if (post.getStatus() == Status.ACTIVE) {
            throw new ActionFailedException(AppCode.POST_CREATION_FAILED);
        }

        post.setStatus(Status.ACTIVE);
        post.setApprovedBy(approvedBy);
        postRepository.save(post);
    }

    @Override
public PaginationWrapper<List<Post>> getFilteredPosts(int page, int size, String status, Boolean approvedOnly) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postId"));

    Page<Post> postPage;

    if ("ACTIVE".equalsIgnoreCase(status) && Boolean.TRUE.equals(approvedOnly)) {
        postPage = postRepository.findByStatusAndApprovedByIsNotNull(Status.ACTIVE, pageable);
    } else {
        postPage = postRepository.findAll(pageable);
    }

    return new PaginationWrapper.Builder<List<Post>>()
            .setData(postPage.getContent())
            .setPaginationInfo(postPage)
            .build();
}

}
