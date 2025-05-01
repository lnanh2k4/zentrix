package com.zentrix.controller;

import com.zentrix.model.entity.Category;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/*
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date April 02, 2025
 */

public class CategoryControllerTest {

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private CategoryService categoryService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllCategories_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Category category = new Category(1, "Electronics", null, null, null);
        Page<Category> categoryPage = new PageImpl<>(List.of(category), pageable, 1);
        when(categoryService.getAllCategories(pageable)).thenReturn(categoryPage);

        // Act
        ResponseEntity<ResponseObject<Page<Category>>> response = categoryController.getAllCategories(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(200, response.getBody().getCode());
        assertEquals(categoryPage, response.getBody().getContent());
        verify(categoryService, times(1)).getAllCategories(pageable);
    }

    @Test
    public void testGetAllCategories_Empty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(categoryService.getAllCategories(pageable)).thenReturn(emptyPage);

        // Act
        ResponseEntity<ResponseObject<Page<Category>>> response = categoryController.getAllCategories(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("No categories found", response.getBody().getMessage());
        assertEquals(200, response.getBody().getCode());
        assertEquals(emptyPage, response.getBody().getContent());
        verify(categoryService, times(1)).getAllCategories(pageable);
    }

    @Test
    public void testGetCategoryById_Success() {
        // Arrange
        int id = 1;
        Category category = new Category(id, "Electronics", null, null, null);
        when(categoryService.getCategoryById(id)).thenReturn(category);

        // Act
        ResponseEntity<ResponseObject<Category>> response = categoryController.getCategoryById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(200, response.getBody().getCode());
        assertEquals(category, response.getBody().getContent());
        verify(categoryService, times(1)).getCategoryById(id);
    }

    @Test
    public void testGetCategoryById_NotFound() {
        // Arrange
        int id = 1;
        when(categoryService.getCategoryById(id)).thenReturn(null);

        // Act
        ResponseEntity<ResponseObject<Category>> response = categoryController.getCategoryById(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Category Exception: Category not found", response.getBody().getMessage());
        assertEquals(1300, response.getBody().getCode());
        assertEquals(null, response.getBody().getContent());
        verify(categoryService, times(1)).getCategoryById(id);
    }

    @Test
    public void testGetSubCategories_Success() {
        // Arrange
        int parentId = 1;
        Category subCategory = new Category(2, "Phones", null, null, null);
        List<Category> subCategories = List.of(subCategory);
        when(categoryService.getSubCategories(parentId)).thenReturn(subCategories);

        // Act
        ResponseEntity<ResponseObject<List<Category>>> response = categoryController.getSubCategories(parentId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(200, response.getBody().getCode());
        assertEquals(subCategories, response.getBody().getContent());
        verify(categoryService, times(1)).getSubCategories(parentId);
    }

    @Test
    public void testGetSubCategories_Empty() {
        // Arrange
        int parentId = 1;
        when(categoryService.getSubCategories(parentId)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ResponseObject<List<Category>>> response = categoryController.getSubCategories(parentId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Category Exception: No subcategories found", response.getBody().getMessage());
        assertEquals(1300, response.getBody().getCode());
        assertEquals(null, response.getBody().getContent());
        verify(categoryService, times(1)).getSubCategories(parentId);
    }
}