package com.zentrix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.ProductTypeAttribute;
import com.zentrix.model.entity.Attribute;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */
public interface ProductTypeAttributeRepository extends JpaRepository<ProductTypeAttribute, Long> {
    /**
     * This method use to find productTypeAttribute by productType id
     * 
     * @param prodTypeId id of productType
     * @return List of the productTypeAttribute by id
     */
    List<ProductTypeAttribute> findByProdTypeId(ProductType prodTypeId);

    /**
     * This method use to check a productTypeAttribute exist by Attribute id
     * 
     * @param atbId Attribute id
     * @return true if that attribute exist in productTypeAttribute
     */
    boolean existsByAtbId(Attribute atbId);

}
