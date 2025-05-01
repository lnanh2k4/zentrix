package com.zentrix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zentrix.model.entity.Stock;
import com.zentrix.model.entity.StockDetail;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
public interface StockDetailRepository extends JpaRepository<StockDetail, Long> {
    /**
     * This method use to find the stockDetail by stock's id
     * 
     * @param stock Stocks
     * @return a list stock detail
     */
    List<StockDetail> findByStockId(Stock stock);
}