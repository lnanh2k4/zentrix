package com.zentrix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zentrix.model.entity.ImagePost;
import com.zentrix.model.entity.Post;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */
public interface ImagePostRepository extends JpaRepository<ImagePost, Long> {

    List<ImagePost> findByPost_PostId(Long postId);

    List<ImagePost> findByImage_ImageId(Long imageId);

    void deleteByPost(Post post);

}
