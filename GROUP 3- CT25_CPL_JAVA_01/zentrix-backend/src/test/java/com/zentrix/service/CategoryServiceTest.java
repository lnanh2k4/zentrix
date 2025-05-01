package com.zentrix.service;

import com.zentrix.model.entity.Category;
import com.zentrix.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date April 02, 2025
 */

public class CategoryServiceTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryRepository categoryRepository;

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
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        // Act
        Page<Category> result = categoryService.getAllCategories(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(category, result.getContent().get(0));
        verify(categoryRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testExistsByCateName_True() {
        // Arrange
        String cateName = "Electronics";
        when(categoryRepository.existsByCateName(cateName)).thenReturn(true);

        // Act
        boolean result = categoryService.existsByCateName(cateName);

        // Assert
        assertTrue(result);
        verify(categoryRepository, times(1)).existsByCateName(cateName);
    }

    @Test
    public void testExistsByCateName_False() {
        // Arrange
        String cateName = "Electronics";
        when(categoryRepository.existsByCateName(cateName)).thenReturn(false);

        // Act
        boolean result = categoryService.existsByCateName(cateName);

        // Assert
        assertFalse(result);
        verify(categoryRepository, times(1)).existsByCateName(cateName);
    }

    @Test
    public void testGetCategoryById_Success() {
        // Arrange
        int id = 1;
        Category category = new Category(id, "Electronics", null, null, null);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        // Act
        Category result = categoryService.getCategoryById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getCateId());
        assertEquals("Electronics", result.getCateName());
        verify(categoryRepository, times(1)).findById(id);
    }

    @Test
    public void testGetCategoryById_NotFound() {
        // Arrange
        int id = 1;
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Category result = categoryService.getCategoryById(id);

        // Assert
        assertNull(result);
        verify(categoryRepository, times(1)).findById(id);
    }

    @Test
    public void testGetSubCategories_Success() {
        // Arrange
        int parentId = 1;
        Category parent = new Category(parentId, "Electronics", null, null, null);
        Category subCategory = new Category(2, "Phones", parent, null, null);
        List<Category> subCategories = List.of(subCategory);
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(categoryRepository.findByParentCateId(parent)).thenReturn(subCategories);

        // Act
        List<Category> result = categoryService.getSubCategories(parentId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(subCategory, result.get(0));
        verify(categoryRepository, times(1)).findById(parentId);
        verify(categoryRepository, times(1)).findByParentCateId(parent);
    }

    @Test
    public void testGetSubCategories_ParentNotFound() {
        // Arrange
        int parentId = 1;
        when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

        // Act
        List<Category> result = categoryService.getSubCategories(parentId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(categoryRepository, times(1)).findById(parentId);
        verify(categoryRepository, never()).findByParentCateId(any());
    }

    @Test
    public void testAddCategory_Success() {
        // Arrange
        Category category = new Category();
        category.setCateName("Electronics");
        category.setParentCateId(null);

        Category savedCategory = new Category();
        savedCategory.setCateId(1);
        savedCategory.setCateName("Electronics");
        savedCategory.setParentCateId(null);

        when(categoryRepository.save(category)).thenReturn(savedCategory);

        // Act
        Category result = categoryService.addCategory(category);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCateId());
        assertEquals("Electronics", result.getCateName());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    public void testAddCategory_ParentNotFound() {
        // Arrange
        Category parent = new Category();
        parent.setCateId(2);

        Category category = new Category();
        category.setCateName("Phones");
        category.setParentCateId(parent);

        when(categoryRepository.findById(2)).thenReturn(Optional.empty());

        // Act
        Category result = categoryService.addCategory(category);

        // Assert
        assertNull(result);
        verify(categoryRepository, times(1)).findById(2);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    public void testUpdateCategory_Success() {
        // Arrange
        int id = 1;
        Category existingCategory = new Category(id, "Electronics", null, null, null);
        Category updatedCategory = new Category(id, "Updated Electronics", null, null, null);

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // Act
        Category result = categoryService.updateCategory(updatedCategory);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getCateId());
        assertEquals("Updated Electronics", result.getCateName());
        verify(categoryRepository, times(1)).findById(id);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    public void testUpdateCategory_NotFound() {
        // Arrange
        int id = 1;
        Category updatedCategory = new Category(id, "Updated Electronics", null, null, null);

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Category result = categoryService.updateCategory(updatedCategory);

        // Assert
        assertNull(result);
        verify(categoryRepository, times(1)).findById(id);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    public void testDeleteCategory_Success() {
        // Arrange
        int id = 1;
        Category category = new Category(id, "Electronics", null, null, null);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentCateId(category)).thenReturn(Collections.emptyList());

       
        

        
    }

    @Test
    public void testDeleteCategory_NotFound() {
        // Arrange
        int id = 1;
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        boolean result = categoryService.deleteCategory(id);

        // Assert
        assertFalse(result);
        verify(categoryRepository, times(1)).findById(id);
        verify(categoryRepository, never()).findByParentCateId(any());
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    public void testDeleteCategory_HasSubCategories() {
        // Arrange
        int id = 1;
        Category category = new Category(id, "Electronics", null, null, null);
        Category subCategory = new Category(2, "Phones", category, null, null);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentCateId(category)).thenReturn(List.of(subCategory));

    }

        
}