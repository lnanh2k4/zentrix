package com.zentrix.controller;

import com.zentrix.model.entity.Supplier;
import com.zentrix.model.request.SupplierRequest;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for managing supplier-related operations, restricted to admin users.
 * Provides endpoints for creating, updating, and deleting suppliers in the dashboard.
 *
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date March 14, 2025
 */
@RequiredArgsConstructor // Automatically generates a constructor for final fields
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // Sets all fields as private and final by default
@RestController // Marks this class as a REST controller to handle HTTP requests
@RequestMapping("/api/v1/dashboard/suppliers") // Base URL path for all endpoints in this controller
@Tag(name = "Supplier Dashboard Controller", description = "Admin-only endpoints for managing suppliers.") // Swagger tag for API documentation
public class SupplierDashboardController {

    SupplierService supplierService; // Service layer dependency for supplier-related business logic

    /**
     * Creates a new supplier in the system.
     *
     * @param supplierRequest The request body containing the details of the supplier to create.
     * @return ResponseEntity containing the created supplier or an error message if creation fails.
     */
    @PreAuthorize("hasRole('ADMIN')") // Restricts access to users with ADMIN role
    @Operation(summary = "Create a new supplier", description = "Adds a new supplier to the system with the provided details.") // Swagger operation annotation
    @PostMapping // Handles POST requests to "/api/v1/dashboard/suppliers"
    public ResponseEntity<ResponseObject<Supplier>> createSupplier(@RequestBody SupplierRequest supplierRequest) {
        // Create a new Supplier entity and populate it with request data
        Supplier newSupplier = new Supplier();
        newSupplier.setSuppName(supplierRequest.getSuppName());
        newSupplier.setEmail(supplierRequest.getEmail());
        newSupplier.setPhone(supplierRequest.getPhone());
        newSupplier.setAddress(supplierRequest.getAddress());

        try {
            // Persist the new supplier via the service layer
            supplierService.addSupplier(newSupplier);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseObject.Builder<Supplier>()
                            .success(true)
                            .message("Supplier Exception: Supplier created successfully")
                            .code(2509) // Custom success code for supplier creation
                            .content(newSupplier) // Newly created supplier entity
                            .build());
        } catch (RuntimeException e) {
            // Handle runtime exceptions (e.g., validation or database errors)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject.Builder<Supplier>()
                            .success(false)
                            .message("Supplier Exception: Failed to create new supplier")
                            .code(2504) // Custom error code for creation failure
                            .content(null)
                            .build());
        }
    }

    /**
     * Updates an existing supplier based on its ID.
     *
     * @param id The ID of the supplier to update, extracted from the URL path.
     * @param supplierRequest The request body containing updated supplier details.
     * @return ResponseEntity containing the updated supplier or an error message if update fails.
     */
    @PreAuthorize("hasRole('ADMIN')") // Restricts access to users with ADMIN role
    @Operation(summary = "Update supplier", description = "Updates an existing supplier based on the given ID.")
    @PutMapping("/{id}") // Handles PUT requests to "/api/v1/dashboard/suppliers/{id}"
    public ResponseEntity<ResponseObject<Supplier>> updateSupplier(
            @PathVariable Integer id, // Supplier ID from the URL path
            @RequestBody SupplierRequest supplierRequest // Updated supplier details
    ) {
        // Attempt to update the supplier via the service layer
        Supplier updatedSupplier = supplierService.updateSupplier(id, supplierRequest);

        if (updatedSupplier == null) {
            // Return 404 Not Found if the supplier doesn't exist or update fails
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject.Builder<Supplier>()
                            .success(false)
                            .message("Supplier Exception: Failed to update supplier")
                            .code(2502) // Custom error code for update failure
                            .content(null)
                            .build());
        }

        // Return 200 OK with the updated supplier details
        return ResponseEntity.ok(new ResponseObject.Builder<Supplier>()
                .success(true)
                .message("Supplier updated successfully")
                .code(HttpStatus.OK.value()) // HTTP status code (200)
                .content(updatedSupplier) // Updated supplier entity
                .build());
    }

    /**
     * Deletes a supplier by its ID.
     *
     * @param id The ID of the supplier to delete, extracted from the URL path.
     * @return ResponseEntity indicating success or failure of the deletion.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a supplier", description = "Removes a supplier from the system based on the given ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteSupplier(@PathVariable int id) {
        try {
            if (supplierService.deleteSupplier(id)) {
                return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                        .success(true)
                        .message("Supplier deleted successfully")
                        .code(HttpStatus.OK.value())
                        .content(null)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject.Builder<Void>()
                                .success(false)
                                .message("Supplier Exception: Supplier not found")
                                .code(2500)
                                .content(null)
                                .build());
            }
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Cannot delete supplier because it is associated with")) {
                if (e.getMessage().contains("products")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ResponseObject.Builder<Void>()
                                    .success(false)
                                    .message(e.getMessage())
                                    .code(2510) // Custom error code for associated products
                                    .content(null)
                                    .build());
                } else if (e.getMessage().contains("stocks")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ResponseObject.Builder<Void>()
                                    .success(false)
                                    .message(e.getMessage())
                                    .code(2512) // Custom error code for associated stocks
                                    .content(null)
                                    .build());
                }
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject.Builder<Void>()
                            .success(false)
                            .message("Supplier Exception: Failed to delete supplier - " + e.getMessage())
                            .code(2503)
                            .content(null)
                            .build());
        }
    }
}