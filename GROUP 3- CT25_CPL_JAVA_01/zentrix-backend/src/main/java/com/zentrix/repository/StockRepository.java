package com.zentrix.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zentrix.model.entity.Stock;
import com.zentrix.model.entity.Supplier;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * Counts the number of stocks associated with a given supplier
     *
     * @param supplier the Supplier entity to check
     * @return the number of stocks associated with the supplier
     */
    long countBySupplierId(Supplier supplier);


}