package com.zentrix.controller;

import com.zentrix.model.entity.Attribute;
import com.zentrix.model.entity.Variation;
import com.zentrix.model.request.AttributeRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.AttributeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attributes")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Attribute Controller", description = "This class contains the attribute CRUD methods.")
public class AttributeController {

    @Autowired
    private AttributeService attributeService;

    /**
     * Retrieves an attribute by ID
     *
     * @param id attribute ID
     * @return ResponseEntity with found attribute
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get attribute", description = "This method is used to get an attribute by attribute ID")

    public ResponseEntity<Attribute> getAttributeById(@Valid @PathVariable Long id) {
        Attribute attribute = attributeService.getById(id);

        if (attribute == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        return ResponseEntity.ok(attribute);
    }

    /**
     * Retrieves all attributes
     *
     * @return ResponseEntity with list of attributes
     */
    @GetMapping
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Get all attributes", description = "This method is usedto getall attributes with pagination")

    public ResponseEntity<ResponseObject<List<Attribute>>> getAllAttributes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size) {

        // Validate pagination parameters
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Page must be >= 0 and size must be > 0");
        }

        // Fetch paginated attributes from the service
        PaginationWrapper<List<Attribute>> wrapper = attributeService.getAll(page,
                size);

        // Build the response object
        ResponseObject<List<Attribute>> response = new ResponseObject.Builder<List<Attribute>>()
                .unwrapPaginationWrapper(wrapper)
                .message("Get all attributes successfully!")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new attribute
     *
     * @param attribute the attribute object to create
     * @return ResponseEntity with the created attribute
     */
    @PostMapping("/createAttribute")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Create Attribute", description = "This method is usedto create a new attribute")

    public ResponseEntity<Attribute> createAttribute(@Valid @RequestBody AttributeRequest request
    // @RequestHeader("Authorization") String jwt
    ) {
        Attribute attribute = attributeService.create(request);
        return ResponseEntity
                .ok(attribute);
    }

    /**
     * Deletes an attribute by ID
     *
     * @param id the attribute ID to delete
     * @return ResponseEntity with deletion status
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Delete attribute", description = "This method is usedto delete an attribute by ID")

    public ResponseEntity<Void> deleteAttribute(@Valid @PathVariable Long id) {
        boolean deleted = attributeService.delete(id);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        return ResponseEntity.ok(null);
    }

    /**
     * Updates an existing attribute by its ID.
     *
     * @param id        the ID of the attribute to update
     * @param attribute the Attribute object containing updated details
     * @return ResponseEntity containing the updated Attribute entity, or NOT_FOUND
     *         if the attribute does not exist
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    @Operation(summary = "Update attribute", description = "This method is used to update an existing attribute")
    public ResponseEntity<Attribute> updateAttribute(
            @Valid @PathVariable("id") Long id,
            @Valid @RequestBody Attribute attribute) {

        attribute.setAtbId(id);
        Attribute updatedAttribute = attributeService.update(id, attribute);

        if (updatedAttribute == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        return ResponseEntity.ok(updatedAttribute);
    }
}