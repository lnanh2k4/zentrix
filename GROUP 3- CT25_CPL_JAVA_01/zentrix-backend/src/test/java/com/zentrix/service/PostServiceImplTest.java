package com.zentrix.service;

import com.zentrix.model.entity.*;
import com.zentrix.model.request.PostRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.utils.Role;
import com.zentrix.model.utils.Status;
import com.zentrix.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock PostRepository postRepository;
    @Mock StaffRepository staffRepository;
    @Mock UserRepository userRepository;
    @Mock ImageRepository imageRepository;
    @Mock ImagePostRepository imagePostRepository;
    @Mock FileServiceImpl fileService;

    @InjectMocks PostServiceImpl postService;

    PostRequest request;
    Post post;
    User user;
    Staff staff;
    Image image;
    MultipartFile file;

    @BeforeEach
    void setup() {
        file = new MockMultipartFile("file.jpg", new byte[1]);

        request = new PostRequest();
        request.setTitle("Title");
        request.setDescription("Description");
        request.setCreatedAt(new Date(System.currentTimeMillis()));
        request.setCreatedBy(1L);
        request.setImageFiles(new MultipartFile[]{file});

        image = Image.builder().imageId(1L).imageLink("http://image.jpg").build();
        staff = new Staff(); staff.setStaffId(1L);
        user = new User(); user.setUserId(1L);
        user.setRoleId(new com.zentrix.model.entity.Role());
        user.getRoleId().setRoleName(Role.ADMIN.name());

        post = Post.builder()
                .postId(1L)
                .title("Title")
                .description("Description")
                .createdBy(staff)
                .createdAt(request.getCreatedAt())
                .status(Status.VERIFYING)
                .build();
    }

    @Test
    void getAllPosts_shouldReturnPaginatedList() {
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.findAll(any(Pageable.class))).thenReturn(page);

        PaginationWrapper<List<Post>> result = postService.getAllPosts(0, 10);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.getData().size())
        );
    }

    @Test
    void getPostById_shouldReturnPostWithImages() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(imagePostRepository.findByPost_PostId(1L)).thenReturn(List.of(
                ImagePost.builder().image(image).post(post).build()
        ));

        Post result = postService.getPostById(1L);
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Title", result.getTitle()),
                () -> assertEquals(1, result.getImages().size())
        );
    }

    @Test
    void getPostById_shouldReturnNullIfNotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        assertNull(postService.getPostById(1L));
    }

    @Test
    void createPost_shouldSucceed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(staffRepository.findStaffByUserId(user)).thenReturn(staff);
        when(postRepository.save(any())).thenReturn(post);
        when(imageRepository.save(any())).thenReturn(image);
        when(fileService.saveFile(any())).thenReturn(image.getImageLink());

        assertDoesNotThrow(() -> postService.createPost(request));
        verify(postRepository).save(any(Post.class));
    }

   

    @Test
    void updatePost_shouldUpdateSuccessfully() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(imagePostRepository.findByPost_PostId(1L)).thenReturn(List.of());
        when(postRepository.save(any())).thenReturn(post);

        Post updated = postService.updatePost(1L, request);

        assertAll(
                () -> assertNotNull(updated),
                () -> assertEquals("Title", updated.getTitle())
        );
    }

    
    @Test
    void deletePost_shouldDeleteSuccessfully() {
        when(postRepository.existsById(1L)).thenReturn(true);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(imagePostRepository.findByPost_PostId(1L)).thenReturn(List.of(
                ImagePost.builder().image(image).post(post).build()
        ));

        assertDoesNotThrow(() -> postService.deletePost(1L));
        verify(postRepository).deleteById(1L);
    }


    @Test
    void approvePost_shouldSucceedIfVerifying() {
        post.setStatus(Status.VERIFYING);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(staffRepository.findStaffByUserId(user)).thenReturn(staff);

        assertDoesNotThrow(() -> postService.approvePost(1L, 1L));
        assertEquals(Status.ACTIVE, post.getStatus());
    }

  

    @Test
    void getFilteredPosts_shouldReturnFilteredIfApproved() {
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.findByStatusAndApprovedByIsNotNull(eq(Status.ACTIVE), any())).thenReturn(page);

        var result = postService.getFilteredPosts(0, 10, "ACTIVE", true);

        assertEquals(1, result.getData().size());
    }
}
