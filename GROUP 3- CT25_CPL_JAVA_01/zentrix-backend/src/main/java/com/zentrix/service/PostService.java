package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.Post;
import com.zentrix.model.request.PostRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 12, 2025
 */

public interface PostService {
    /**
     * Retrieves all posts.
     * 
     * @return A list of all available posts.
     */
    PaginationWrapper<List<Post>> getAllPosts(int page, int size);

    /**
     * Retrieves a post by its unique ID.
     * 
     * @param postId The ID of the post to retrieve.
     * @return The Post object if found, otherwise null or an exception.
     */
    public Post getPostById(Long postId);

    /**
     * Creates a new post using the provided request data.
     * 
     * @param postRequest The request object containing post details.
     * @return The created Post object.
     */
    public void createPost(PostRequest postRequest);

    /**
     * Updates an existing post with new data.
     * 
     * @param postId      The ID of the post to update.
     * @param postRequest The request object containing updated post details.
     * @return The updated Post object.
     */
    public Post updatePost(Long postId, PostRequest postRequest);

    /**
     * Deletes a post by its ID.
     * 
     * @param postId The ID of the post to delete.
     *               If the post does not exist, an exception may be thrown.
     */
    public void deletePost(Long postId);

    /**
     * Approves a post by setting its status to approved and recording the approver.
     *
     * @param postId       The ID of the post to be approved.
     * @param approvedById The ID of the user (admin/staff) who approves the post.
     */
    public void approvePost(Long postId, Long approvedById);

    /**
     * Retrieves a paginated list of posts filtered by status and approval
     * condition.
     *
     * @param page         The current page number (starting from 0).
     * @param size         The number of items per page.
     * @param status       The status to filter posts by (e.g., "ACTIVE", "PENDING",
     *                     etc.).
     * @param approvedOnly If true, only approved posts will be returned; if false,
     *                     includes all.
     * @return A paginated wrapper containing the list of filtered posts.
     */
    public PaginationWrapper<List<Post>> getFilteredPosts(int page, int size, String status, Boolean approvedOnly);
}
