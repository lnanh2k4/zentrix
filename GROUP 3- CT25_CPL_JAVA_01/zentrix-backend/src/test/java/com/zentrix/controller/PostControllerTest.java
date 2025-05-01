package com.zentrix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zentrix.model.entity.Post;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.ImagePostRepository;
import com.zentrix.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpStatusCodeException;

import java.sql.Date;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date April 07, 2025
 */
@ExtendWith(MockitoExtension.class)
public class PostControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PostService postService;

    @Mock
    private ImagePostRepository imagePostRepository;

    @InjectMocks
    private PostController postController;

    private Post post;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(postController).build();

        post = Post.builder()
                .postId(1L)
                .title("Samsung Galaxy S24 Ultra")
                .description("High-end smartphone with AI camera.")
                .createdAt(new Date(System.currentTimeMillis()))
                .build();
    }

    @Test
    void testGetPostById_Success() throws Exception {
        when(postService.getPostById(1L)).thenReturn(post);

        mockMvc.perform(get("/api/v1/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.content.title").value("Samsung Galaxy S24 Ultra"));

        verify(postService, times(1)).getPostById(1L);
    }
    @Test
    void testGetPostById_NotFound() throws Exception {
        HttpStatusCodeException ex = mock(HttpStatusCodeException.class);
        when(ex.getMessage()).thenReturn("Post not found");
        when(postService.getPostById(99L)).thenThrow(ex);

        mockMvc.perform(get("/api/v1/posts/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Post not found"));

        verify(postService, times(1)).getPostById(99L);
    }
}