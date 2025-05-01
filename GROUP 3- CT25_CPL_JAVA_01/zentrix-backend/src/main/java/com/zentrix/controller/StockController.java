package com.zentrix.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;

import com.zentrix.model.entity.Stock;
import com.zentrix.model.request.CreateStockRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.StockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/*
* @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
* @date February 13, 2025
*/
@RestController
@RequestMapping("/api/v1/stocks")
@Tag(name = "Stock Controller")
public class StockController {

    @Autowired
    private StockService stockService;

    @PostMapping("/create")
    /**
     * This method is used to create a new stock
     *
     * @param request stock import
     * @return stock
     */
    @Operation(summary = "Create Stock", description = "This method is used to create a new stock")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    public ResponseEntity<Stock> createStock(@Valid @RequestBody CreateStockRequest request) {
        Stock stock = stockService.createStock(request);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/{id}")
    /**
     * This method is used to get a stock by Id
     *
     * @param id id of a stock
     * @return stock has been found with id
     */
    @Operation(summary = "Get Stock By Id", description = "This method is used toget astock by ID")
    @PreAuthorize("hasRole('WAREHOUSE STAFF')")
    public ResponseEntity<Stock> getStockById(@PathVariable Long id) {
        Stock stock = stockService.getStockById(id);
        if (stock != null) {
            return ResponseEntity.ok(stock);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    /**
     * This method is used to get all stocks
     *
     * @return List of all stock available
     */
    @GetMapping
    @Operation(summary = "Get all Stocks", description = "This method is used toget allstocks with pagination")
    @PreAuthorize("hasAnyRole('WAREHOUSE STAFF')")
    public ResponseEntity<ResponseObject<List<Stock>>> getAllStocks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size) {
        PaginationWrapper<List<Stock>> wrapper = stockService.getAllStocks(page,
                size);

        ResponseObject<List<Stock>> response = new ResponseObject.Builder<List<Stock>>()
                .unwrapPaginationWrapper(wrapper)
                .message("List of all stocks retrieved successfully!")
                .code(HttpStatus.OK.value())
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }
}
