package com.zentrix.controller;

import com.zentrix.model.entity.Supplier;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.SupplierService;
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
 * Controller class for handling HTTP requests related to supplier data.
 * Provides public endpoints for retrieving supplier information.
 *
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date March 14, 2025
 */
@RequiredArgsConstructor // Automatically generates a constructor for final fields
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // Sets all fields as private and final by default
@RestController // Marks this class as a REST controller to handle HTTP requests
@RequestMapping("/api/v1/suppliers") // Base URL path for all endpoints in this controller
@Tag(name = "Supplier Controller", description = "Public endpoints for reading suppliers.") // Swagger tag for API documentation
public class SupplierController {

    SupplierService supplierService; // Service layer dependency for supplier-related business logic

    /**
     * Retrieves a paginated list of all suppliers.
     *
     * @param page The page number to retrieve (default is 0).
     * @param size The number of items per page (default is 10).
     * @return ResponseEntity containing a paginated list of suppliers wrapped in a ResponseObject.
     */
    @PreAuthorize("permitAll()") // Allows access to all users without authentication
    @Operation(summary = "Retrieve paginated suppliers", description = "Fetches a paginated list of suppliers.") // Swagger operation annotation
    @GetMapping // Handles HTTP GET requests to "/api/v1/suppliers"
    public ResponseEntity<ResponseObject<Page<Supplier>>> getAllSuppliers(
            @RequestParam(defaultValue = "0") int page, // Page number for pagination
            @RequestParam(defaultValue = "10") int size // Number of items per page
    ) {
        // Create a Pageable object for pagination
        Pageable pageable = PageRequest.of(page, size);

        // Fetch paginated supplier data from the service layer
        Page<Supplier> suppliers = supplierService.getAllSuppliers(pageable);

        // Build and return a response with a dynamic message based on result
        return ResponseEntity.ok(new ResponseObject.Builder<Page<Supplier>>()
                .success(true)
                .message(suppliers.isEmpty() ? "No suppliers found" : "Success") // Conditional message
                .code(HttpStatus.OK.value()) // HTTP status code (200)
                .content(suppliers) // Paginated supplier data
                .build());
    }

    /**
     * Retrieves a specific supplier by its ID.
     *
     * @param id The ID of the supplier to retrieve, extracted from the URL path.
     * @return ResponseEntity containing the supplier details or an error message if not found.
     */
    @PreAuthorize("permitAll()") // Allows access to all users
    @Operation(summary = "Retrieve a supplier by ID", description = "Fetches details of a supplier by its ID.")
    @GetMapping("/{id}") // Handles GET requests to "/api/v1/suppliers/{id}"
    public ResponseEntity<ResponseObject<Supplier>> getSupplierById(@PathVariable int id) {
        // Fetch the supplier from the service layer
        Supplier supplier = supplierService.getSupplierById(id);

        if (supplier == null) {
            // Return 404 Not Found if the supplier doesn't exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject.Builder<Supplier>()
                            .success(false)
                            .message("Supplier Exception: Supplier not found")
                            .code(2500) // Custom error code for supplier not found
                            .content(null)
                            .build());
        }

        // Return 200 OK with the supplier details
        return ResponseEntity.ok(new ResponseObject.Builder<Supplier>()
                .success(true)
                .message("Success")
                .code(HttpStatus.OK.value()) // HTTP status code (200)
                .content(supplier) // Supplier entity
                .build());
    }

    /**
     * Searches for suppliers by name using a partial match.
     *
     * @param name The search string to match against supplier names, provided as a query parameter.
     * @return ResponseEntity containing a list of matching suppliers or an error message if none found.
     */
    @PreAuthorize("permitAll()") // Allows access to all users
    @Operation(summary = "Search suppliers by name", description = "Fetches suppliers whose names contain the given search string.")
    @GetMapping("/search") // Handles GET requests to "/api/v1/suppliers/search?name={name}"
    public ResponseEntity<ResponseObject<List<Supplier>>> searchSuppliersByName(@RequestParam String name) {
        // Fetch suppliers matching the name from the service layer
        List<Supplier> suppliers = supplierService.findSuppliersByName(name);

        if (suppliers.isEmpty()) {
            // Return 404 Not Found if no suppliers match the search criteria
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject.Builder<List<Supplier>>()
                            .success(false)
                            .message("Supplier Exception: No suppliers found with the given name")
                            .code(2500) // Custom error code for no matching suppliers
                            .content(null)
                            .build());
        }

        // Return 200 OK with the list of matching suppliers
        return ResponseEntity.ok(new ResponseObject.Builder<List<Supplier>>()
                .success(true)
                .message("Success")
                .code(HttpStatus.OK.value()) // HTTP status code (200)
                .content(suppliers) // List of supplier entities
                .build());
    }
}