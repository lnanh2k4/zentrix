package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.ProductTypeVariation;
import com.zentrix.model.request.ProductTypeVariationRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
public interface ProductTypeVariationService {

    public boolean isVariationUsed(Long variId);

    /**
     * Retrieves all ProductTypeVariation records.
     *
     * @return A list of all ProductTypeVariation entities.
     */
    PaginationWrapper<List<ProductTypeVariation>> getAll(int page, int size);

    /**
     * Retrieves a ProductTypeVariation by its ID.
     *
     * @param id The ID of the ProductTypeVariation.
     * @return The ProductTypeVariation entity or null if not found.
     */
    ProductTypeVariation getById(Long id);

    /**
     * Creates and saves a new ProductTypeVariation entity.
     *
     * @param productTypeVariation The ProductTypeVariation entity to create.
     * @return The saved ProductTypeVariation entity.
     */
    ProductTypeVariation create(ProductTypeVariationRequest productTypeVariation);

    /**
     * Updates an existing ProductTypeVariation by its ID.
     * If the entity exists, it updates the record; otherwise, it returns null.
     *
     * @param id                   The ID of the ProductTypeVariation to update.
     * @param productTypeVariation The new ProductTypeVariation details.
     * @return The updated ProductTypeVariation entity or null if the ID does not
     *         exist.
     */
    ProductTypeVariation update(Long id, ProductTypeVariationRequest productTypeVariation);

    /**
     * Deletes a ProductTypeVariation by its ID.
     *
     * @param id The ID of the ProductTypeVariation to delete.
     */
    boolean delete(Long id);
}
