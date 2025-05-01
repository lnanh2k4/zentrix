package com.zentrix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.ProductTypeVariation;
import com.zentrix.model.entity.Variation;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */
public interface ProductTypeVariationRepository extends JpaRepository<ProductTypeVariation, Long> {
    /**
     * Finds a list of ProductTypeVariation entities associated with the given
     * ProductType entity.
     *
     * @param prodTypeId the ProductType entity to search for Ascertain
     * @return a list of ProductTypeVariation entities linked to the specified
     *         ProductType
     */
    List<ProductTypeVariation> findByProdTypeId(ProductType prodTypeId);

    /**
     * Checks if any ProductTypeVariation entity exists with the given Variation
     * entity.
     *
     * @param variId the Variation entity to check
     * @return true if at least one ProductTypeVariation exists with the specified
     *         Variation, false otherwise
     */
    boolean existsByVariId(Variation variId);
}
