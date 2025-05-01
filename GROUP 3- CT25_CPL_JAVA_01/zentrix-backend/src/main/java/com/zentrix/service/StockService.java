package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.Stock;
import com.zentrix.model.request.CreateStockRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
public interface StockService {
    /**
     * This method uses to create a new Stock
     * 
     * @param request information of a stock
     * @return stock has been created
     */
    Stock createStock(CreateStockRequest request);

    /**
     * This method uses to get a stock by id
     * 
     * @param id id of a stock
     * @return stock has been found
     */
    Stock getStockById(Long id);

    /**
     * This method uses to get all stocks
     * 
     * @return list of all stock
     */
    PaginationWrapper<List<Stock>> getAllStocks(int page, int size);
}