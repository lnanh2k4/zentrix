package com.zentrix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.zentrix.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Repository interface for managing Category entities in the database.
 * Extends JpaRepository to provide basic CRUD operations and custom query methods.
 *
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date February 12, 2025
 */
@Repository // Marks this interface as a Spring Data repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * Retrieves a list of categories that have the specified parent category.
     *
     * @param parentCateId The parent Category entity to filter subcategories by.
     * @return A List of Category entities that are children of the given parent category.
     */
    List<Category> findByParentCateId(Category parentCateId);

    /**
     * Retrieves a paginated list of all categories.
     *
     * @param pageable Pagination and sorting information (e.g., page number, size, sort order).
     * @return A Page object containing a list of Category entities for the specified page.
     */
    Page<Category> findAll(Pageable pageable);

    /**
     * Checks if a category with the specified name exists in the database.
     *
     * @param cateName The name of the category to check for existence.
     * @return true if a category with the given name exists, false otherwise.
     */
    boolean existsByCateName(String cateName);
}