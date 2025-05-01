package com.zentrix.controller;

import com.zentrix.model.entity.Variation;
import com.zentrix.model.request.VariationRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.VariationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/variations")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Variation Controller", description = "This class contains the variation CRUD methods.")
public class VariationController {

    @Autowired
    private VariationService variationService;

    /**
     * Retrieves a variation by ID
     *
     * @param id variation ID
     * @return ResponseEntity with found variation
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get variation", description = "This method is used to get a by variation ID")

    public ResponseEntity<Variation> getVariationById(@Valid @PathVariable Long id) {
        Variation variation = variationService.getById(id);

        if (variation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        return ResponseEntity.ok(variation);
    }

    /**
     * Retrieves all variations
     *
     * @return ResponseEntity with list of variations
     */
    @GetMapping
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get all variations", description = "This method is usedto getall variations with pagination")

    public ResponseEntity<ResponseObject<List<Variation>>> getAllVariations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size) {

        // Fetch paginated variations from the service
        PaginationWrapper<List<Variation>> wrapper = variationService.getAll(page,
                size);

        // Build the response object
        ResponseObject<List<Variation>> response = new ResponseObject.Builder<List<Variation>>()
                .unwrapPaginationWrapper(wrapper)
                .message("Get all variations successfully!")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new variation
     *
     * @param variation the variation object to create
     * @return ResponseEntity with the created variation
     */
    @PostMapping("/createVariation")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Create variation", description = "This method is used to create a new variation")
    public ResponseEntity<Variation> createVariation(@Valid @RequestBody VariationRequest variation) {
        try {
            Variation createdVariation = variationService.create(variation);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdVariation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Updates an existing variation
     *
     * @param id        the variation ID to update
     * @param variation the updated variation object
     * @return ResponseEntity with the updated variation
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Update variation", description = "This method is usedto updatean existing variation")

    public ResponseEntity<Variation> updateVariation(
            @Valid @PathVariable Long id,
            @Valid @RequestBody Variation variation) {
        variation.setVariId(id); // Ensure the ID matches the path variable
        Variation updatedVariation = variationService.update(id, variation);

        if (updatedVariation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        return ResponseEntity
                .ok(updatedVariation);
    }

    /**
     * Deletes a variation by ID
     *
     * @param id the variation ID to delete
     * @return ResponseEntity with deletion status
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Delete variation", description = "This method is usedto delete a variation by ID")

    public ResponseEntity<Void> deleteVariation(@Valid @PathVariable Long id) {
        boolean deleted = variationService.delete(id);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        return ResponseEntity.ok(null);
    }
}