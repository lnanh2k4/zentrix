package com.zentrix.controller;

import com.zentrix.model.entity.Category;
import com.zentrix.model.request.CategoryRequest;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for managing category-related operations, restricted to admin users.
 * Provides endpoints for creating, updating, and deleting categories in the dashboard.
 *
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date March 14, 2025
 */
@RequiredArgsConstructor // Automatically generates a constructor for final fields
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // Sets all fields as private and final by default
@RestController // Marks this class as a REST controller to handle HTTP requests
@RequestMapping("/api/v1/dashboard/categories") // Base URL path for all endpoints in this controller
@Tag(name = "Category Dashboard Controller", description = "Admin-only endpoints for managing categories") // Swagger tag for API documentation
public class CategoryDashboardController {

    CategoryService categoryService; // Service layer dependency for category-related business logic

    /**
     * Creates a new category in the system.
     *
     * @param category The request body containing the details of the category to create.
     * @return ResponseEntity containing the created category or an error message if creation fails.
     */
    @PreAuthorize("hasRole('ADMIN')") // Restricts access to users with ADMIN role
    @Operation(summary = "Create a new category", description = "Adds a new category to the system.") // Swagger operation annotation
    @PostMapping // Handles POST requests to "/api/v1/dashboard/categories"
    public ResponseEntity<ResponseObject<Category>> createCategory(@RequestBody CategoryRequest category) {
        // Validate that the category name is neither null nor empty
        if (category.getCateName() == null || category.getCateName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject.Builder<Category>()
                            .success(false)
                            .message("Category Exception: Invalid category name")
                            .code(1306) // Custom error code for invalid name
                            .content(null)
                            .build());
        }

        // Trim the category name and validate it contains only alphanumeric characters and spaces
        String cateName = category.getCateName().trim();
        if (!cateName.matches("^[a-zA-Z0-9\\s]+$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject.Builder<Category>()
                            .success(false)
                            .message("Category Exception: Category name cannot contain special characters")
                            .code(1310) // Custom error code for special characters
                            .content(null)
                            .build());
        }

        // Check if the category name already exists
        if (categoryService.existsByCateName(cateName)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseObject.Builder<Category>()
                            .success(false)
                            .message("Category Exception: Category name already exists")
                            .code(1311) // Custom error code for duplicate name
                            .content(null)
                            .build());
        }

        // Create a new Category entity
        Category createCategory = new Category();
        createCategory.setCateName(cateName);

        // Handle optional parent category ID
        if (category.getParentCateId() != null) {
            Category subCategory = categoryService.getCategoryById(category.getParentCateId());
            if (subCategory == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject.Builder<Category>()
                                .success(false)
                                .message("Category Exception: Parent category not found")
                                .code(1307) // Custom error code for parent not found
                                .content(null)
                                .build());
            }
            createCategory.setParentCateId(subCategory); // Set the parent category
        }

        try {
            // Persist the new category via the service layer
            categoryService.addCategory(createCategory);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseObject.Builder<Category>()
                            .success(true)
                            .message("Category Exception: Category created successfully")
                            .code(1309) // Custom success code
                            .content(createCategory)
                            .build());
        } catch (Exception e) {
            // Handle unexpected errors during creation
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject.Builder<Category>()
                            .success(false)
                            .message("Category Exception: Failed to create new category")
                            .code(1302) // Custom error code for creation failure
                            .content(null)
                            .build());
        }
    }

    /**
     * Updates an existing category based on its ID.
     *
     * @param id The ID of the category to update, extracted from the URL path.
     * @param category The request body containing updated category details.
     * @return ResponseEntity containing the updated category or an error message if update fails.
     */
    @PreAuthorize("hasRole('ADMIN')") // Restricts access to users with ADMIN role
    @Operation(summary = "Update an existing category", description = "Modifies an existing category based on the given ID.")
    @PutMapping("/{id}") // Handles PUT requests to "/api/v1/dashboard/categories/{id}"
    public ResponseEntity<ResponseObject<Category>> updateCategory(@PathVariable int id,
                                                                  @RequestBody CategoryRequest category) {
        try {
            // Fetch the existing category
            Category updateCategory = categoryService.getCategoryById(id);
            if (updateCategory == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject.Builder<Category>()
                                .success(false)
                                .message("Category Exception: Category not found")
                                .code(1300) // Custom error code for category not found
                                .content(null)
                                .build());
            }

            // Update the category name
            updateCategory.setCateName(category.getCateName());

            // Handle optional parent category ID
            Integer parentCateId = category.getParentCateId();
            if (parentCateId != null) {
                // Prevent a category from being its own parent
                if (parentCateId.equals(id)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ResponseObject.Builder<Category>()
                                    .success(false)
                                    .message("Category Exception: A category cannot be its own parent")
                                    .code(1309) // Custom error code for self-reference
                                    .content(null)
                                    .build());
                }

                // Fetch and validate the parent category
                Category parentCategory = categoryService.getCategoryById(parentCateId);
                if (parentCategory == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ResponseObject.Builder<Category>()
                                    .success(false)
                                    .message("Category Exception: Parent category not found")
                                    .code(1307) // Custom error code for parent not found
                                    .content(null)
                                    .build());
                }

                // Prevent setting a descendant as the parent (avoid circular references)
                if (isDescendant(parentCategory, id, categoryService)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ResponseObject.Builder<Category>()
                                    .success(false)
                                    .message("Category Exception: Cannot set a descendant category as parent")
                                    .code(1309) // Custom error code for descendant conflict
                                    .content(null)
                                    .build());
                }

                updateCategory.setParentCateId(parentCategory); // Set the new parent category
            }

            // Persist the updated category
            categoryService.updateCategory(updateCategory);
            return ResponseEntity.ok(new ResponseObject.Builder<Category>()
                    .success(true)
                    .message("Category updated successfully")
                    .code(HttpStatus.OK.value()) // HTTP status code (200)
                    .content(updateCategory)
                    .build());
        } catch (Exception e) {
            // Handle unexpected errors during update
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject.Builder<Category>()
                            .success(false)
                            .message("Category Exception: Failed to update category")
                            .code(1303) // Custom error code for update failure
                            .content(null)
                            .build());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a category", description = "Removes a category from the system based on the given ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteCategory(@PathVariable int id) {
        try {
            boolean isDeleted = categoryService.deleteCategory(id);
            if (!isDeleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject.Builder<Void>()
                                .success(false)
                                .message("Category Exception: Category not found")
                                .code(1300)
                                .content(null)
                                .build());
            }

            return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                    .success(true)
                    .message("Category deleted successfully")
                    .code(1309)
                    .content(null)
                    .build());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Cannot delete category because it is associated with")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseObject.Builder<Void>()
                                .success(false)
                                .message(e.getMessage())
                                .code(1312) // Custom error code for associated products
                                .content(null)
                                .build());
            }
            if (e.getMessage().contains("Cannot delete category because it has")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseObject.Builder<Void>()
                                .success(false)
                                .message(e.getMessage())
                                .code(1313) // Custom error code for subcategories
                                .content(null)
                                .build());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject.Builder<Void>()
                            .success(false)
                            .message("Category Exception: Failed to delete category")
                            .code(1304)
                            .content(null)
                            .build());
        }
    }

    /**
     * Checks if a given category is a descendant of another category to prevent circular references.
     *
     * @param parentCategory The potential parent category to check.
     * @param childId The ID of the category being updated (potential child).
     * @param categoryService The service layer to fetch category data.
     * @return true if the parentCategory is a descendant of the category with childId, false otherwise.
     */
    private boolean isDescendant(Category parentCategory, int childId, CategoryService categoryService) {
        Category current = parentCategory;
        // Traverse the parent hierarchy
        while (current.getParentCateId() != null) {
            if (current.getParentCateId().getCateId() == childId) {
                return true; // Circular reference detected
            }
            // Move up the hierarchy
            current = categoryService.getCategoryById(current.getParentCateId().getCateId());
        }
        return false; // No circular reference found
    }
}