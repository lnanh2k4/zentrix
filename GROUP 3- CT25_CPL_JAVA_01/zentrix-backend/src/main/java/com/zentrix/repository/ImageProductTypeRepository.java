package com.zentrix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zentrix.model.entity.ImageProductType;
import com.zentrix.model.entity.ProductType;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */
public interface ImageProductTypeRepository extends JpaRepository<ImageProductType, Long> {
    List<ImageProductType> findByProdTypeId(ProductType prodTypeId);

    void deleteByProdTypeId(ProductType prodTypeId);
}
