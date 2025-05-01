package com.zentrix.controller;

import com.zentrix.model.entity.Branch;
import com.zentrix.model.entity.ProductTypeBranch;
import com.zentrix.model.request.BranchRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controller class responsible for handling HTTP requests related to branch management.
 *
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
@RequiredArgsConstructor // Automatically generates a constructor for final fields
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // Sets all fields as private and final by default
@RestController // Indicates this class is a REST controller handling HTTP requests
@RequestMapping("/api/v1/branches") // Base URL path for all endpoints in this controller
@Tag(name = "Branch Controller", description = "Handles operations related to branches.") // Swagger tag for API documentation
public class BranchController {

    BranchService branchService; // Service layer dependency for branch-related business logic

    /**
     * Retrieves a paginated list of all branches.
     *
     * @param page The page number to retrieve (default is 0).
     * @param size The number of items per page (default is 10).
     * @return ResponseEntity containing a paginated list of branches wrapped in a ResponseObject.
     */
    @PreAuthorize("permitAll()") // Allows access to all users without authentication
    @Operation(summary = "Retrieve all branches", description = "Fetches a paginated list of all available branches.") // Swagger operation annotation
    @GetMapping // Handles HTTP GET requests to "/api/v1/branches"
    public ResponseEntity<ResponseObject<List<Branch>>> getAllBranches(
            @RequestParam(value = "page", defaultValue = "0") int page, // Page number for pagination
            @RequestParam(value = "size", defaultValue = "10") int size // Number of items per page
    ) {
        // Fetch paginated branch data from the service layer
        PaginationWrapper<List<Branch>> branches = branchService.getAllBranches(page, size);

        // Build and return a successful response with pagination details
        ResponseObject<List<Branch>> response = new ResponseObject.Builder<List<Branch>>()
                .unwrapPaginationWrapper(branches) // Extracts pagination metadata and content
                .message("List of all branches retrieved successfully") // Success message
                .code(HttpStatus.OK.value()) // HTTP status code (200)
                .success(true) // Indicates the operation was successful
                .build();

        return ResponseEntity.ok(response); // Return HTTP 200 OK with response object
    }

    /**
     * Retrieves a specific branch by its ID.
     *
     * @param id The ID of the branch to retrieve, extracted from the URL path.
     * @return ResponseEntity containing the branch details or an error message if not found.
     */
    @PreAuthorize("permitAll()") // Allows access to all users
    @Operation(summary = "Retrieve a branch by ID", description = "Fetches the details of a specific branch based on the given ID.")
    @GetMapping("/{id}") // Handles GET requests to "/api/v1/branches/{id}"
    public ResponseEntity<ResponseObject<Branch>> getBranchById(
            @PathVariable Long id // Branch ID from the URL path
    ) {
        try {
            // Attempt to fetch the branch from the service layer
            Branch branch = branchService.getBranchById(id);

            if (branch == null) {
                // Return 404 Not Found if the branch doesn't exist
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject.Builder<Branch>()
                                .success(false)
                                .message("Branch Exception: Branch not found")
                                .code(1100) // Custom error code for branch not found
                                .content(null)
                                .build());
            }

            // Return 200 OK with the branch details
            return ResponseEntity.ok(new ResponseObject.Builder<Branch>()
                    .success(true)
                    .message("Success")
                    .code(HttpStatus.OK.value())
                    .content(branch)
                    .build());
        } catch (Exception e) {
            // Handle unexpected errors and return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject.Builder<Branch>()
                            .success(false)
                            .message("Branch Exception: Internal server error while processing branch")
                            .code(1105) // Custom error code for internal error
                            .content(null)
                            .build());
        }
    }

    /**
     * Retrieves all branches without pagination, but includes pagination metadata.
     *
     * @return ResponseEntity containing a list of all branches with pagination metadata.
     */
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get all branches without pagination", description = "Retrieve all branches without pagination but with pagination metadata.")
    @GetMapping("/all") // Handles GET requests to "/api/v1/branches/all"
    public ResponseEntity<ResponseObject<List<Branch>>> getAllBranchesNonPaged() {
        // Fetch all branches from the service layer
        List<Branch> branches = branchService.getAllBranchesNonPaged();

        // Calculate pagination metadata (single page with all items)
        int totalElements = branches.size();
        int pageSize = totalElements;
        int totalPages = 1;

        // Wrap the list in a PaginationWrapper for consistency
        PaginationWrapper<List<Branch>> pagination = new PaginationWrapper<>(
                branches, 0, pageSize, totalPages, totalElements
        );

        // Build and return a successful response
        ResponseObject<List<Branch>> response = new ResponseObject.Builder<List<Branch>>()
                .unwrapPaginationWrapper(pagination)
                .message("List of all branches retrieved successfully")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();

        return ResponseEntity.ok(response); // Return HTTP 200 OK
    }

    /**
     * Searches for branches by name using a partial match (case-insensitive).
     *
     * @param name The keyword to search for in branch names, extracted from the URL path.
     * @return ResponseEntity containing a list of matching branches or an error if none found.
     */
    @PreAuthorize("permitAll()")
    @Operation(summary = "Retrieve branches by name", description = "Fetches a list of branches whose names contain the given keyword (case-insensitive).")
    @GetMapping("/search/{name}") // Handles GET requests to "/api/v1/branches/search/{name}"
    public ResponseEntity<ResponseObject<List<Branch>>> getBranchByName(@PathVariable String name) {
        // Validate the search keyword
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject.Builder<List<Branch>>()
                            .success(false)
                            .message("Branch Exception: Search keyword cannot be empty")
                            .code(1101) // Custom error code for invalid input
                            .content(null)
                            .build());
        }

        // Fetch branches matching the name from the service layer
        List<Branch> branches = branchService.findBranchesByName(name);

        if (branches.isEmpty()) {
            // Return 404 Not Found if no branches match the keyword
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject.Builder<List<Branch>>()
                            .success(false)
                            .message("Branch Exception: No branches found with the given name")
                            .code(1100)
                            .content(null)
                            .build());
        }

        // Return 200 OK with the matching branches
        return ResponseEntity.ok(new ResponseObject.Builder<List<Branch>>()
                .success(true)
                .message("Branch Exception: Success")
                .code(1102)
                .content(branches)
                .build());
    }

    /**
     * Creates a new branch in the system.
     *
     * @param branchRequest The request body containing branch details.
     * @param bindingResult Validation result for the request body.
     * @return ResponseEntity containing the created branch or an error message.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new branch", description = "Adds a new branch to the system.")
    @PostMapping // Handles POST requests to "/api/v1/branches"
    public ResponseEntity<ResponseObject<Branch>> createBranch(
            @Valid @RequestBody BranchRequest branchRequest, // Validated request body
            BindingResult bindingResult // Contains validation errors, if any
    ) {
        // Check for validation errors in the request body
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject.Builder<Branch>()
                            .success(false)
                            .message("Branch Exception: " + errorMessage)
                            .code(1102)
                            .content(null)
                            .build());
        }

        String branchName = branchRequest.getBrchName();

        // Check if the branch name already exists
        try {
            List<Branch> existingBranches = branchService.findBranchesByName(branchName);
            if (!existingBranches.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ResponseObject.Builder<Branch>()
                                .success(false)
                                .message("Branch Exception: Branch name '" + branchName + "' is already taken")
                                .code(1106) // Custom error code for duplicate name
                                .content(null)
                                .build());
            }
        } catch (Exception e) {
            // Log or handle service layer errors if necessary (currently ignored)
        }

        // Validate branch name for special characters
        if (branchName != null && branchName.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject.Builder<Branch>()
                            .success(false)
                            .message("Branch Exception: Branch name '" + branchName + "' contains invalid special characters (e.g., #, @, $, etc.)")
                            .code(1107) // Custom error code for invalid characters
                            .content(null)
                            .build());
        }

        // Create a new Branch entity from the request data
        Branch newBranch = new Branch();
        newBranch.setBrchName(branchRequest.getBrchName());
        newBranch.setAddress(branchRequest.getAddress());
        newBranch.setPhone(branchRequest.getPhone()); // No validation applied to phone
        newBranch.setStatus(branchRequest.getStatus());

        try {
            // Save the new branch via the service layer
            Branch savedBranch = branchService.createBranch(newBranch);
            return ResponseEntity.ok(new ResponseObject.Builder<Branch>()
                    .success(true)
                    .message("Branch created successfully")
                    .code(1109) // Custom success code
                    .content(savedBranch)
                    .build());
        } catch (DataIntegrityViolationException e) {
            // Handle database constraint violations (e.g., unique constraints)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject.Builder<Branch>()
                            .success(false)
                            .message("Branch Exception: Data constraint violation")
                            .code(1102)
                            .content(null)
                            .build());
        } catch (Exception e) {
            // Handle unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject.Builder<Branch>()
                            .success(false)
                            .message("Branch Exception: Internal server error while processing branch")
                            .code(1105)
                            .content(null)
                            .build());
        }
    }

    /**
     * Updates an existing branch based on its ID.
     *
     * @param id The ID of the branch to update, extracted from the URL path.
     * @param branchRequest The request body containing updated branch details.
     * @return ResponseEntity containing the updated branch or an error message.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing branch", description = "Modifies an existing branch based on the given ID.")
    @PutMapping("/{id}") // Handles PUT requests to "/api/v1/branches/{id}"
    public ResponseEntity<ResponseObject<Branch>> updateBranch(
            @PathVariable Long id, // Branch ID from the URL
            @RequestBody BranchRequest branchRequest // Updated branch details
    ) {
        try {
            // Fetch the existing branch
            Branch existingBranch = branchService.getBranchById(id);
            if (existingBranch == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject.Builder<Branch>()
                                .success(false)
                                .message("Branch Exception: Branch not found")
                                .code(1100)
                                .content(null)
                                .build());
            }

            String newBranchName = branchRequest.getBrchName();

            // Check for duplicate branch name (excluding the current branch)
            Branch duplicateBranch = branchService.findByBrchName(newBranchName);
            if (duplicateBranch != null && !duplicateBranch.getBrchId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ResponseObject.Builder<Branch>()
                                .success(false)
                                .message("Branch Exception: Branch name '" + newBranchName + "' is already taken")
                                .code(1106)
                                .content(null)
                                .build());
            }

            // Validate branch name for special characters
            if (newBranchName != null && newBranchName.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseObject.Builder<Branch>()
                                .success(false)
                                .message("Branch Exception: Branch name '" + newBranchName + "' contains invalid special characters (e.g., #, @, $, etc.)")
                                .code(1107)
                                .content(null)
                                .build());
            }

            // Update the existing branch with new data
            existingBranch.setBrchName(newBranchName);
            existingBranch.setAddress(branchRequest.getAddress());
            existingBranch.setPhone(branchRequest.getPhone()); // No validation applied
            existingBranch.setStatus(branchRequest.getStatus());

            try {
                // Persist the updated branch
                branchService.updateBranch(id, existingBranch);
                return ResponseEntity.ok(new ResponseObject.Builder<Branch>()
                        .success(true)
                        .message("Branch updated successfully")
                        .code(HttpStatus.OK.value())
                        .content(existingBranch)
                        .build());
            } catch (Exception e) {
                // Handle update failure
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ResponseObject.Builder<Branch>()
                                .success(false)
                                .message("Branch Exception: Failed to update branch")
                                .code(1103) // Custom error code for update failure
                                .content(null)
                                .build());
            }

        } catch (Exception e) {
            // Handle unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject.Builder<Branch>()
                            .success(false)
                            .message("Branch Exception: Internal server error while processing branch")
                            .code(1105)
                            .content(null)
                            .build());
        }
    }

    /**
     * Deletes a branch by its ID.
     *
     * @param id The ID of the branch to delete, extracted from the URL path.
     * @return ResponseEntity indicating success or failure of the deletion.
     */
    @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Delete a branch", description = "Removes a branch from the system based on the given ID.")
        @DeleteMapping("/{id}")
        public ResponseEntity<ResponseObject<Void>> deleteBranch(@PathVariable Long id) {
        try {
                boolean isDeleted = branchService.deleteBranch(id);

                if (isDeleted) {
                return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                        .success(true)
                        .message("Branch deleted successfully")
                        .code(1109)
                        .content(null)
                        .build());
                } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject.Builder<Void>()
                                .success(false)
                                .message("Branch Exception: Branch not found")
                                .code(1100)
                                .content(null)
                                .build());
                }
        } catch (IllegalStateException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseObject.Builder<Void>()
                                .success(false)
                                .message("Branch Exception: " + e.getMessage())
                                .code(1111) // Mã lỗi mới cho trường hợp có sản phẩm
                                .content(null)
                                .build());
        } catch (ResponseStatusException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject.Builder<Void>()
                                .success(false)
                                .message("Branch Exception: " + e.getReason())
                                .code(1100)
                                .content(null)
                                .build());
        } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ResponseObject.Builder<Void>()
                                .success(false)
                                .message("Branch Exception: Failed to delete branch")
                                .code(1104)
                                .content(null)
                                .build());
        }
}
        /**
         * Retrieves the product types available in a specific branch along with their quantities.
         *
         * @param branchId The ID of the branch to check product quantities for, extracted from the URL path.
         * @return ResponseEntity containing a list of product types with quantities and status messages.
         */
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Retrieve product types in a branch", description = "Fetches all product types available in a specific branch with their quantities and stock status.")
        @GetMapping("/{branchId}/product-types") // Handles GET requests to "/api/v1/branches/{branchId}/product-types"
        public ResponseEntity<ResponseObject<List<ProductTypeBranch>>> getProductTypesInBranch(
                @PathVariable("branchId") Long branchId // Branch ID from the URL path
        ) {
        try {
                // Fetch the branch to ensure it exists
                Branch branch = branchService.getBranchById(branchId);
                if (branch == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject.Builder<List<ProductTypeBranch>>()
                                .success(false)
                                .message("Branch Exception: Branch not found")
                                .code(1100) // Custom error code for branch not found
                                .content(null)
                                .build());
                }

                // Fetch product types for the branch from the service layer
                List<ProductTypeBranch> productTypes = branchService.getProductTypesByBranchId(branchId);

                if (productTypes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject.Builder<List<ProductTypeBranch>>()
                                .success(false)
                                .message("No product types found in this branch")
                                .code(1110) // Custom error code for no products
                                .content(null)
                                .build());
                }

                // Add stock status to each product type (optional enhancement)
                productTypes.forEach(pt -> {
                int quantity = pt.getQuantity();
                if (quantity == 0) {
                        pt.setStatusMessage("Out of stock"); // Out of stock
                } else if (quantity <= 50) {
                        pt.setStatusMessage("Low stock"); // Low stock
                } else {
                        pt.setStatusMessage("In stock"); // In stock
                }
                });

                // Build and return a successful response
                ResponseObject<List<ProductTypeBranch>> response = new ResponseObject.Builder<List<ProductTypeBranch>>()
                        .success(true)
                        .message("Product types in branch retrieved successfully")
                        .code(HttpStatus.OK.value())
                        .content(productTypes)
                        .build();

                return ResponseEntity.ok(response); // Return HTTP 200 OK
        } catch (Exception e) {
                // Handle unexpected errors
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ResponseObject.Builder<List<ProductTypeBranch>>()
                                .success(false)
                                .message("Branch Exception: Internal server error while retrieving product types")
                                .code(1105) // Custom error code for internal error
                                .content(null)
                                .build());
        }
        }
}