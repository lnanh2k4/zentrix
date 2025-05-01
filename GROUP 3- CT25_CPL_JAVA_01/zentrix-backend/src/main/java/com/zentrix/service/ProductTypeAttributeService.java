package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.ProductTypeAttribute;
import com.zentrix.model.request.ProductTypeAttributeRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
public interface ProductTypeAttributeService {
    public boolean isAttributeUsed(Long atbId);

    /**
     * Retrieves all ProductTypeAttribute records.
     *
     * @return A list of all ProductTypeAttribute entities.
     */
    PaginationWrapper<List<ProductTypeAttribute>> getAll(int page, int size);

    /**
     * Retrieves a ProductTypeAttribute by its ID.
     *
     * @param id The ID of the ProductTypeAttribute.
     * @return The ProductTypeAttribute or null if not found.
     */
    ProductTypeAttribute getById(Long id);

    /**
     * Creates and saves a new ProductTypeAttribute.
     *
     * @param productTypeAttribute The ProductTypeAttribute entity to create.
     * @return The saved ProductTypeAttribute entity.
     */
    ProductTypeAttribute create(ProductTypeAttributeRequest productTypeAttribute);

    /**
     * Updates an existing ProductTypeAttribute by its ID.
     * If the entity exists, it updates the record; otherwise, it returns null.
     *
     * @param id                   The ID of the ProductTypeAttribute to update.
     * @param productTypeAttribute The new ProductTypeAttribute details.
     * @return The updated ProductTypeAttribute or null if the ID does not exist.
     */
    ProductTypeAttribute update(Long id, ProductTypeAttributeRequest productTypeAttribute);

    /**
     * Deletes a ProductTypeAttribute by its ID.
     *
     * @param id The ID of the ProductTypeAttribute to delete.
     */
    boolean delete(Long id);
}
