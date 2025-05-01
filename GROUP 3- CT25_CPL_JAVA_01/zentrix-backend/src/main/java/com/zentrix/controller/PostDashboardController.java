package com.zentrix.controller;

import com.zentrix.model.entity.ImagePost;
import com.zentrix.model.entity.Post;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.PostRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.repository.ImagePostRepository;
import com.zentrix.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date April 01, 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard/posts")
@Tag(name = "Admin Post Controller", description = "Admin-only endpoints for managing posts")
public class PostDashboardController {

    @Autowired
    private PostService postService;
    @Autowired
    private ImagePostRepository imagePostRepository;

    /**
     * Retrieves all posts.
     *
     * @param jwt The authorization token.
     * @return ApiResponse containing the list of all posts.
     */
    @Operation(summary = "Get all posts", description = "Retrieve a list of all posts.")
    @GetMapping("")
    public ResponseEntity<ResponseObject<List<Post>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginationWrapper<List<Post>> wrapper = postService.getAllPosts(page, size);

        for (Post post : wrapper.getData()) {
            List<ImagePost> imagePosts = imagePostRepository.findByPost_PostId(post.getPostId());

            List<String> imageLinks = imagePosts.stream()
                    .map(imagePost -> imagePost.getImage().getImageLink())
                    .toList();

            post.setImages(imageLinks);
        }

        ResponseObject<List<Post>> response = new ResponseObject.Builder<List<Post>>()
                .unwrapPaginationWrapper(wrapper)
                .message("Get posts list successfully!")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a post by its ID.
     *
     * @param postId The ID of the post to retrieve.
     * @param jwt    The authorization token.
     * @return ApiResponse containing the requested post.
     */
    @Operation(summary = "Get post by ID", description = "Retrieve details of a specific post using its ID.")
    @GetMapping("/{postId}")
    public ResponseEntity<ResponseObject<?>> getPostById(@PathVariable Long postId) {
        try {
            return ResponseEntity.ok(
                    new ResponseObject.Builder<Post>()
                            .content(postService.getPostById(postId))
                            .message("Post retrieved successfully by ID.")
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
     * Creates a new post.
     *
     * @param postRequest     Data for the new post.
     * @param bindingResult   Validation result.
     * @return Response with success or validation error message.
     */
    @PreAuthorize("hasRole('SELLER_STAFF') or hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<ResponseObject<?>> createPost(
            @Valid @ModelAttribute PostRequest postRequest,
            BindingResult bindingResult) {

        Map<String, String> errors = validatePost(postRequest, bindingResult);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseObject.Builder<>()
                    .code(AppCode.FIELD_NOT_VALID.getCode())
                    .success(false)
                    .message("Validation failed")
                    .content(errors)
                    .build());
        }

        postService.createPost(postRequest);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .code(200).success(true).message("Post created successfully").content(null).build());
    }
    /**
     * Updates an existing post.
     *
     * @param postId          ID of the post to update.
     * @param postRequest     Updated data.
     * @param bindingResult   Validation result.
     * @return Response with updated post or validation errors.
     */
    @PutMapping("/update/{postId}")
    public ResponseEntity<ResponseObject<?>> updatePost(
            @PathVariable Long postId,
            @Valid @ModelAttribute PostRequest postRequest,
            BindingResult bindingResult) {

        Map<String, String> errors = validatePost(postRequest, bindingResult);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseObject.Builder<>()
                    .code(AppCode.FIELD_NOT_VALID.getCode())
                    .success(false)
                    .message("Validation failed")
                    .content(errors)
                    .build());
        }

        Post post = postService.updatePost(postId, postRequest);
        return ResponseEntity.ok(new ResponseObject.Builder<>()
                .content(post)
                .message("Post updated successfully")
                .code(HttpStatus.OK.value())
                .success(true)
                .build());
    }
    /**
     * Deletes a post by ID.
     *
     * @param postId ID of the post to delete.
     * @return Confirmation message.
     */
    @DeleteMapping("/remove/{postId}")
    public ResponseEntity<ResponseObject<?>> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .message("Delete post successfully")
                .code(HttpStatus.OK.value())
                .success(true)
                .build());
    }
    /**
     * Validates post request fields.
     *
     * @param postRequest   The post request.
     * @param bindingResult The result of field validations.
     * @return A map of validation errors.
     */
    private Map<String, String> validatePost(PostRequest postRequest, BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        }
        if (postRequest.getTitle() == null || postRequest.getTitle().trim().isEmpty()) {
            errors.put("title", AppCode.POST_TITLE_REQUIRED.getMessage());
        } else if (postRequest.getTitle().length() > 255) {
            errors.put("title", AppCode.POST_TITLE_TOO_LONG.getMessage());
        }
        if (postRequest.getDescription() != null && postRequest.getDescription().trim().isEmpty()) {
            errors.put("description", AppCode.POST_DESCRIPTION_BLANK.getMessage());
        }
        if (postRequest.getCreatedAt() != null && postRequest.getCreatedAt().after(new Date())) {
            errors.put("createdAt", AppCode.POST_CREATED_AT_IN_FUTURE.getMessage());
        }

        return errors;
    }

    /**
     * Approves a post.
     *
     * @param postId        ID of the post to approve.
     * @param approvedById  ID of the staff user who approves the post.
     * @return Success response after approval.
     */
    @Operation(summary = "Approve a post by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully approved post"),
            @ApiResponse(responseCode = "404", description = "Post or staff not found"),
            @ApiResponse(responseCode = "400", description = "Post already approved"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    /**
     * 
     * @param postId
     * @param approvedById
     * @return
     */
    @PatchMapping("/{postId}/approve")
    public ResponseEntity<ResponseObject> approvePost(
            @Parameter(description = "ID of the post to approve", example = "1") @PathVariable Long postId,
            @Parameter(description = "ID of the staff approving the post", example = "1") @RequestParam Long approvedById) {
        postService.approvePost(postId, approvedById);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .message("Post approved successfully")
                .code(HttpStatus.OK.value())
                .success(true)
                .build());
    }
}
