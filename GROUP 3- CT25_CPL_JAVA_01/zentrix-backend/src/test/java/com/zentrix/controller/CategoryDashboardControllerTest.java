package com.zentrix.controller;

import com.zentrix.model.entity.Category;
import com.zentrix.model.request.CategoryRequest;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CategoryDashboardControllerTest {

    @InjectMocks
    private CategoryDashboardController categoryDashboardController;

    @Mock
    private CategoryService categoryService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateCategory_Success() {
        // Arrange
        CategoryRequest request = new CategoryRequest("Electronics", null);

        // The Category object that will be returned by the service (simulating database save)
        Category savedCategory = new Category();
        savedCategory.setCateId(1); // Simulate database auto-increment
        savedCategory.setCateName("Electronics");
        savedCategory.setParentCateId(null);

        // Mock the service behavior
        when(categoryService.existsByCateName("Electronics")).thenReturn(false);
        when(categoryService.addCategory(any(Category.class))).thenReturn(savedCategory);

        // Act
        ResponseEntity<ResponseObject<Category>> response = categoryDashboardController.createCategory(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Category Exception: Category created successfully", response.getBody().getMessage());
        assertEquals(1309, response.getBody().getCode());       
        verify(categoryService, times(1)).existsByCateName("Electronics");
        verify(categoryService, times(1)).addCategory(any(Category.class));
    }

    @Test
    public void testCreateCategory_NameExists() {
        // Arrange
        CategoryRequest request = new CategoryRequest("Electronics", null);
        when(categoryService.existsByCateName("Electronics")).thenReturn(true);

        // Act
        ResponseEntity<ResponseObject<Category>> response = categoryDashboardController.createCategory(request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Category Exception: Category name already exists", response.getBody().getMessage());
        assertEquals(1311, response.getBody().getCode());
        assertEquals(null, response.getBody().getContent());
        verify(categoryService, times(1)).existsByCateName("Electronics");
        verify(categoryService, never()).addCategory(any());
    }

    @Test
    public void testUpdateCategory_Success() {
        // Arrange
        int id = 1;
        CategoryRequest request = new CategoryRequest("Updated Electronics", null);
        Category existingCategory = new Category(id, "Electronics", null, null, null);
        Category updatedCategory = new Category(id, "Updated Electronics", null, null, null);
        when(categoryService.getCategoryById(id)).thenReturn(existingCategory);
        when(categoryService.updateCategory(any(Category.class))).thenReturn(updatedCategory);

        // Act
        ResponseEntity<ResponseObject<Category>> response = categoryDashboardController.updateCategory(id, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Category updated successfully", response.getBody().getMessage());
        assertEquals(200, response.getBody().getCode());
        assertEquals(updatedCategory, response.getBody().getContent());
        verify(categoryService, times(1)).getCategoryById(id);
        verify(categoryService, times(1)).updateCategory(any(Category.class));
    }

    @Test
    public void testUpdateCategory_NotFound() {
        // Arrange
        int id = 1;
        CategoryRequest request = new CategoryRequest("Updated Electronics", null);
        when(categoryService.getCategoryById(id)).thenReturn(null);

        // Act
        ResponseEntity<ResponseObject<Category>> response = categoryDashboardController.updateCategory(id, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Category Exception: Category not found", response.getBody().getMessage());
        assertEquals(1300, response.getBody().getCode());
        assertEquals(null, response.getBody().getContent());
        verify(categoryService, times(1)).getCategoryById(id);
        verify(categoryService, never()).updateCategory(any());
    }

    @Test
    public void testDeleteCategory_Success() {
        // Arrange
        int id = 1;
        when(categoryService.deleteCategory(id)).thenReturn(true);

        // Act
        ResponseEntity<ResponseObject<Void>> response = categoryDashboardController.deleteCategory(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Category deleted successfully", response.getBody().getMessage());
        assertEquals(1309, response.getBody().getCode());
        assertEquals(null, response.getBody().getContent());
        verify(categoryService, times(1)).deleteCategory(id);
    }

    @Test
    public void testDeleteCategory_NotFound() {
        // Arrange
        int id = 1;
        when(categoryService.deleteCategory(id)).thenReturn(false);

        // Act
        ResponseEntity<ResponseObject<Void>> response = categoryDashboardController.deleteCategory(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Category Exception: Category not found", response.getBody().getMessage());
        assertEquals(1300, response.getBody().getCode());
        assertEquals(null, response.getBody().getContent());
        verify(categoryService, times(1)).deleteCategory(id);
    }
}