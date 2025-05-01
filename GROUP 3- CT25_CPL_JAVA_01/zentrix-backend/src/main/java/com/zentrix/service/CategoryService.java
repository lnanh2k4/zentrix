package com.zentrix.service;

import com.zentrix.model.entity.Category;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining business logic for managing Category entities.
 * Provides methods for retrieving, creating, updating, and deleting categories.
 *
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date February 12, 2025
 */
public interface CategoryService {

    /**
     * Checks if a category with the specified name exists in the system.
     *
     * @param cateName The name of the category to check for existence.
     * @return true if a category with the given name exists, false otherwise.
     */
    boolean existsByCateName(String cateName);

    /**
     * Retrieves a paginated list of all categories.
     *
     * @param pageable Pagination and sorting information (e.g., page number, size, sort order).
     * @return A Page object containing a list of Category entities for the specified page.
     */
    Page<Category> getAllCategories(Pageable pageable);

    /**
     * Retrieves a specific category by its ID.
     *
     * @param id The unique identifier of the category to retrieve.
     * @return The Category entity with the specified ID, or null if not found.
     */
    Category getCategoryById(int id);

    /**
     * Retrieves a list of subcategories for a given parent category ID.
     *
     * @param parentId The ID of the parent category whose subcategories are to be retrieved.
     * @return A List of Category entities that are children of the specified parent category.
     */
    List<Category> getSubCategories(int parentId);

    /**
     * Adds a new category to the system.
     *
     * @param category The Category entity to be created.
     * @return The created Category entity, typically with an assigned ID.
     */
    Category addCategory(Category category);

    /**
     * Updates an existing category.
     *
     * @param category The Category entity containing updated details.
     * @return The updated Category entity, or null if the category was not found.
     */
    Category updateCategory(Category category);

    /**
     * Deletes a category by its ID.
     *
     * @param id The unique identifier of the category to delete.
     * @return true if the category was successfully deleted, false if it was not found.
     */
    boolean deleteCategory(int id);
}