package com.zentrix.controller;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.repository.ImagePostRepository;
import com.zentrix.model.entity.ImagePost;
import com.zentrix.model.entity.Post;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/*
* @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
* @date February 12, 2025
*/
@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Guest Post Controller", description = "Public endpoints for reading posts")
public class PostController {

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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean approvedOnly) {
    
        PaginationWrapper<List<Post>> wrapper = postService.getFilteredPosts(page, size, status, approvedOnly);
    
        for (Post post : wrapper.getData()) {
            List<ImagePost> imagePosts = imagePostRepository.findByPost_PostId(post.getPostId());
            List<String> imageLinks = imagePosts.stream()
                    .map(imagePost -> imagePost.getImage().getImageLink())
                    .toList(); 
            post.setImages(imageLinks);
        }
    
        ResponseObject<List<Post>> response = new ResponseObject.Builder<List<Post>>()
                .unwrapPaginationWrapper(wrapper)
                .message("Filtered post list successfully!")
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

}
