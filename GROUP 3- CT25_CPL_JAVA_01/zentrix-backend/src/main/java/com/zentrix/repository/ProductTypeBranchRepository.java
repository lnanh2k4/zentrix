package com.zentrix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zentrix.model.entity.Branch;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.ProductTypeBranch;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 14, 2025
 */
@Repository
public interface ProductTypeBranchRepository extends JpaRepository<ProductTypeBranch, Long> {
    /**
     * Finds a list of ProductTypeBranch entities associated with the given
     * ProductType entity.
     *
     * @param productType the ProductType entity to search for
     * @return a list of ProductTypeBranch entities linked to the specified
     *         ProductType
     */
    List<ProductTypeBranch> findByProdTypeId(ProductType productType);

    /**
     * Finds a list of ProductTypeBranch entities by the specified product type ID.
     *
     * @param prodTypeId the ID of the product type to search for
     * @return a list of ProductTypeBranch entities associated with the given
     *         product type ID
     */
    List<ProductTypeBranch> findByProdTypeIdProdTypeId(Long prodTypeId);

    /**
     * Finds a specific ProductTypeBranch entity by the given ProductType and Branch
     * entities.
     *
     * @param prodTypeId the ProductType entity representing the product type
     * @param brchId     the Branch entity representing the branch
     * @return the ProductTypeBranch entity matching the given ProductType and
     *         Branch, or null if not found
     */
    ProductTypeBranch findByProdTypeIdAndBrchId(ProductType prodTypeId, Branch brchId);

    /**
     * Finds a specific ProductTypeBranch entity by product type ID and branch ID
     * using a custom JPQL query.
     *
     * @param prodTypeId the ID of the product type
     * @param brchId     the ID of the branch
     * @return the ProductTypeBranch entity matching the given product type ID and
     *         branch ID, or null if not found
     */
    @Query("SELECT ptb FROM ProductTypeBranch ptb WHERE ptb.prodTypeId.prodTypeId = :prodTypeId AND ptb.brchId.brchId = :brchId")
    ProductTypeBranch findByProdTypeIdProdTypeIdAndBrchIdBrchId(@Param("prodTypeId") Long prodTypeId,
            @Param("brchId") Long brchId);

    /**
     * Finds all product types associated with a specific branch.
     *
     * @param branchId The ID of the branch.
     * @return A List of ProductTypeBranch entities.
     */
    List<ProductTypeBranch> findByBrchId_BrchId(Long branchId);

    boolean existsByBrchId_BrchId(Long branchId);
}
