package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.ProductType;
import com.zentrix.model.request.ProductTypeRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
* @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
* @date February 18, 2025
*/
public interface ProductTypeService {

    /**
     * Saves a new ProductType or updates an existing one.
     *
     * @param productType The ProductType entity to save.
     * @return The saved ProductType entity.
     */
    ProductType saveProductType(ProductTypeRequest productTypeRequest);

    /**
     * Finds a ProductType by its ID.
     *
     * @param id The ID of the ProductType.
     * @return An Optional containing the ProductType if found, otherwise empty.
     */
    ProductType findProductTypeById(Long id);

    ProductType updateProductType(Long id, ProductTypeRequest request);

    /**
     * Retrieves all ProductType records from the database.
     *
     * @return A list of all ProductType entities.
     */
    PaginationWrapper<List<ProductType>> getAllProductTypes(int page, int size);

    /**
     * Deletes a ProductType by its ID.
     *
     * @param id The ID of the ProductType to delete.
     */
    boolean deleteProductType(Long id);

    /**
     * Find a productType by productType Id
     *
     * @param prodId The ID of the ProductType to find
     */
    List<ProductType> findProductTypesByProductId(Long prodId);

    /**
     * Set unavailable a productType
     *
     * @param id The ID of the ProductType to set unavailable
     */
    boolean unactiveProductType(Long id, Integer status);

    /**
     * This method uses to search a productType by name
     * 
     * @param keyword name of a productType
     * @param page    current page
     * @param size    amount element a page
     * @return a productType has been found by name
     */
    public PaginationWrapper<List<ProductType>> searchProductTypesByName(String keyword, int page, int size);
}
