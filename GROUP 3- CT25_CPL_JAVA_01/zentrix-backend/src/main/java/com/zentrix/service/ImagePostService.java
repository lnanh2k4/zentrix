package com.zentrix.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.zentrix.model.entity.ImagePost;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */
public interface ImagePostService {
    /**
     * Uploads an image for a specific post.
     *
     * @param postId The ID of the post to associate with the image.
     * @param file   The image file to be uploaded.
     * @return The saved ImagePost entity.
     */
    ImagePost uploadPostImage(Long postId, MultipartFile file);

    /**
     * Retrieves all images associated with a given post.
     *
     * @param postId The ID of the post.
     * @return A list of ImagePost entities linked to the post.`
     */
    List<ImagePost> getImagesByPostId(Long postId);

    /**
     * Retrieves all posts associated with a given image.
     *
     * @param imageId The ID of the image.
     * @return A list of ImagePost entities linked to the image.
     */
    List<ImagePost> getPostsByImageId(Long imageId);

    /**
     * Deletes an ImagePost entry from the database.
     *
     * @param imagePostId The ID of the ImagePost entry to delete.
     */
    void deleteImagePost(Long imagePostId);
}
