package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.StockDetail;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
public interface StockDetailService {
    /**
     * This method uses to create a stockDetail
     * 
     * @param stockDetail information of stockDetail
     * @return Stockdetail has been created
     */
    StockDetail createStockDetail(StockDetail stockDetail);

    /**
     * This method uses to get a stockDetail by Id
     * 
     * @param id id of stockDetail
     * @return StockDetail
     */
    StockDetail getStockDetailById(Long id);

    /**
     * This method uses to get all StockDetail by stockId
     * 
     * @param stockId Id of stock
     * @return List of all stockDetail has been found by stockId
     */
    List<StockDetail> getStockDetailsByStockId(Long stockId);

    /**
     * This method uses to update a stockDetail
     * 
     * @param id          id of a stockDetail
     * @param stockDetail Information of stockdetail need to change
     * @return a Stockdetail has been updated
     */
    StockDetail updateStockDetail(Long id, StockDetail stockDetail);

    /**
     * This method uses to delete a stockDetail
     * 
     * @param id id of a stock
     * @return true if delete completed, false if stockDetail not found
     */
    boolean deleteStockDetail(Long id);
}
