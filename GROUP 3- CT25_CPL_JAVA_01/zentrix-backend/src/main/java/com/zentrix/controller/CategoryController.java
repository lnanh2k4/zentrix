package com.zentrix.controller;

import com.zentrix.model.entity.Category;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class responsible for handling HTTP requests related to category management.
 * Provides public endpoints for retrieving category data.
 *
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date March 14, 2025
 */
@RequiredArgsConstructor // Automatically generates a constructor for final fields
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // Sets all fields as private and final by default
@RestController // Marks this class as a REST controller to handle HTTP requests
@RequestMapping("/api/v1/categories") // Base URL path for all endpoints in this controller
@Tag(name = "Category Controller", description = "Public endpoints for reading categories") // Swagger tag for API documentation
public class CategoryController {

    CategoryService categoryService; // Service layer dependency for category-related business logic

    /**
     * Retrieves a paginated list of all categories.
     *
     * @param page The page number to retrieve (default is 0).
     * @param size The number of items per page (default is 10).
     * @return ResponseEntity containing a paginated list of categories wrapped in a ResponseObject.
     */
    @PreAuthorize("permitAll()") // Allows access to all users without authentication
    @Operation(summary = "Retrieve paginated categories", description = "Fetches a paginated list of categories.") // Swagger operation annotation
    @GetMapping("") // Handles HTTP GET requests to "/api/v1/categories"
    public ResponseEntity<ResponseObject<Page<Category>>> getAllCategories(
            @RequestParam(defaultValue = "0") int page, // Page number for pagination
            @RequestParam(defaultValue = "10") int size // Number of items per page
    ) {
        // Create a Pageable object for pagination
        Pageable pageable = PageRequest.of(page, size);

        // Fetch paginated category data from the service layer
        Page<Category> categories = categoryService.getAllCategories(pageable);

        // Build and return a response with dynamic message based on result
        return ResponseEntity.ok(new ResponseObject.Builder<Page<Category>>()
                .success(true)
                .message(categories.isEmpty() ? "No categories found" : "Success") // Conditional message
                .code(HttpStatus.OK.value()) // HTTP status code (200)
                .content(categories) // Paginated category data
                .build());
    }

    /**
     * Retrieves a specific category by its ID.
     *
     * @param id The ID of the category to retrieve, extracted from the URL path.
     * @return ResponseEntity containing the category details or an error message if not found.
     */
    @PreAuthorize("permitAll()") // Allows access to all users
    @Operation(summary = "Retrieve a category by ID", description = "Fetches a specific category's details based on the given ID.")
    @GetMapping("/{id}") // Handles GET requests to "/api/v1/categories/{id}"
    public ResponseEntity<ResponseObject<Category>> getCategoryById(@PathVariable int id) {
        // Fetch the category from the service layer
        Category category = categoryService.getCategoryById(id);

        if (category == null) {
            // Return 404 Not Found if the category doesn't exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject.Builder<Category>()
                            .success(false)
                            .message("Category Exception: Category not found")
                            .code(1300) // Custom error code for category not found
                            .content(null)
                            .build());
        }

        // Return 200 OK with the category details
        return ResponseEntity.ok(new ResponseObject.Builder<Category>()
                .success(true)
                .message("Success")
                .code(HttpStatus.OK.value()) // HTTP status code (200)
                .content(category) // Category entity
                .build());
    }

    /**
     * Retrieves a list of subcategories for a given parent category ID.
     *
     * @param parentId The ID of the parent category, extracted from the URL path.
     * @return ResponseEntity containing a list of subcategories or an error message if none found.
     */
    @PreAuthorize("permitAll()") // Allows access to all users
    @Operation(summary = "Retrieve subcategories", description = "Fetches a list of subcategories based on the given parent category ID.")
    @GetMapping("/sub/{parentId}") // Handles GET requests to "/api/v1/categories/sub/{parentId}"
    public ResponseEntity<ResponseObject<List<Category>>> getSubCategories(@PathVariable int parentId) {
        // Fetch subcategories from the service layer
        List<Category> subCategories = categoryService.getSubCategories(parentId);

        if (subCategories.isEmpty()) {
            // Return 404 Not Found if no subcategories exist for the parent ID
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject.Builder<List<Category>>()
                            .success(false)
                            .message("Category Exception: No subcategories found")
                            .code(1300) // Custom error code for no subcategories
                            .content(null)
                            .build());
        }

        // Return 200 OK with the list of subcategories
        return ResponseEntity.ok(new ResponseObject.Builder<List<Category>>()
                .success(true)
                .message("Success")
                .code(HttpStatus.OK.value()) // HTTP status code (200)
                .content(subCategories) // List of subcategory entities
                .build());
    }
}