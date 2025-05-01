package com.zentrix.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zentrix.model.entity.Product;
import com.zentrix.model.entity.ProductType;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 14, 2025
 */
@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
    /**
     * Finds a list of ProductType entities associated with the given Product
     * entity.
     *
     * @param prodId the Product entity to search for
     * @return a list of ProductType entities linked to the specified Product
     */
    List<ProductType> findByProdId(Product prodId);

    /**
     * Finds a ProductType entity by its ID.
     *
     * @param id the ID of the ProductType to search for
     * @return an Optional containing the ProductType entity if found, or an empty
     *         Optional if not found
     */
    Optional<ProductType> findById(long id);

    /**
     * This method use to find productType by name
     * 
     * @param prodTypeName name of productType
     * @return List of the productType has been found by name
     */
    Page<ProductType> findByProdTypeNameContainingIgnoreCase(String prodTypeName, Pageable pageable);

}
