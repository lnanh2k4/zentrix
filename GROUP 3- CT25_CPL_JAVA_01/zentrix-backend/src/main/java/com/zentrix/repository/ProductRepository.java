package com.zentrix.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.zentrix.model.entity.Category;
import com.zentrix.model.entity.Product;
import com.zentrix.model.entity.Supplier;
import com.zentrix.model.entity.ProductType;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

        Page<Product> findByStatusIn(List<Integer> statusList, Pageable pageable);

        /**
         * This method use to find the product by category id
         * 
         * @param cateId category id of product
         * @return List of the product has been found by category id
         */
        List<Product> findByCateId_CateId(Long cateId);

        /**
         * This method use to find the supplier by supplier ID
         * 
         * @param suppId supplier id of product
         * @return List of the product has been found by supplier id
         */
        List<Product> findBySuppId_SuppId(Integer suppId);

        /**
         * This method use to find product by name
         * 
         * @param prodName name of product
         * @return List of the product has been found by name
         */
        Page<Product> findByProdNameContainingIgnoreCase(String prodName, Pageable pageable);

        long countByCateId(Category category);

        long countBySuppId(Supplier supplier);

        @Query("SELECT p FROM Product p JOIN p.productTypes pt WHERE pt.prodTypeId = :prodTypeId")
        Product findByProductTypeId(Long prodTypeId);
}
