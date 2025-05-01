package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.ProductTypeBranch;
import com.zentrix.model.request.ProductTypeBranchRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 14, 2025
 */
public interface ProductTypeBranchService {

    /**
     * Saves or updates a ProductTypeBranch in the database.
     *
     * @param productTypeBranch The ProductTypeBranch object to be saved.
     * @return The saved ProductTypeBranch object.
     */
    ProductTypeBranch saveProductTypeBranch(ProductTypeBranchRequest productTypeBranch);

    /**
     * Finds a ProductTypeBranch by its ID.
     *
     * @param id The ID of the ProductTypeBranch to be retrieved.
     * @return An Optional containing the ProductTypeBranch if found, otherwise an
     *         empty Optional.
     */
    ProductTypeBranch findProductTypeBranchById(Long id);

    /**
     * Retrieves a list of all ProductTypeBranches in the system.
     *
     * @return A list of all ProductTypeBranches.
     */
    PaginationWrapper<List<ProductTypeBranch>> getAllProductTypeBranches(int page, int size);

    /**
     * Deletes a ProductTypeBranch from the database based on its ID.
     *
     * @param id The ID of the ProductTypeBranch to be deleted.
     */
    boolean deleteProductTypeBranch(Long id);

    /**
     * Updates an existing ProductTypeBranch in the database.
     *
     * @param id      The ID of the ProductTypeBranch to update.
     * @param request The request object containing updated data.
     * @return The updated ProductTypeBranch object.
     */
    ProductTypeBranch updateProductTypeBranch(Long id, ProductTypeBranchRequest request);

    /**
     * Finds a list of ProductTypeBranch entities associated with the given
     * ProductType.
     *
     * @param productType the ProductType entity to search for
     * @return a list of ProductTypeBranch entities linked to the specified
     *         ProductType
     */
    List<ProductTypeBranch> findByProdTypeId(ProductType productType);

    /**
     * Retrieves all ProductTypeBranch entities by the specified product type ID.
     *
     * @param id the ID of the product type to search for
     * @return a list of ProductTypeBranch entities associated with the given
     *         product type ID
     */
    List<ProductTypeBranch> getProductTypeBranchByProdTypeId(Long id);

    /**
     * Finds a specific ProductTypeBranch entity by product type ID and branch ID.
     *
     * @param prodTypeId the ID of the product type
     * @param brchId     the ID of the branch
     * @return the ProductTypeBranch entity matching the given product type ID and
     *         branch ID, or null if not found
     */
    ProductTypeBranch findByProdTypeIdAndBrchId(Long prodTypeId, Long brchId);
}