package com.zentrix.controller;

import com.zentrix.model.entity.Stock;
import com.zentrix.model.request.CreateStockRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockController stockController;

    @BeforeEach
    void setUp() {
        reset(stockService);
    }

    // Test for createStock
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createStock_Success() {
        CreateStockRequest request = new CreateStockRequest();
        Stock stock = new Stock();

        when(stockService.createStock(request)).thenReturn(stock);

        ResponseEntity<Stock> response = stockController.createStock(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(stock, response.getBody());
        verify(stockService).createStock(request);
    }

    // Test for getStockById
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getStockById_Success() {
        Long id = 1L;
        Stock stock = new Stock();

        when(stockService.getStockById(id)).thenReturn(stock);

        ResponseEntity<Stock> response = stockController.getStockById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(stock, response.getBody());
        verify(stockService).getStockById(id);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getStockById_NotFound() {
        Long id = 1L;

        when(stockService.getStockById(id)).thenReturn(null);

        ResponseEntity<Stock> response = stockController.getStockById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(stockService).getStockById(id);
    }

    // Test for getAllStocks
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getAllStocks_Success() {
        List<Stock> stocks = Arrays.asList(new Stock());
        PaginationWrapper<List<Stock>> wrapper = new PaginationWrapper.Builder<List<Stock>>()
                .setData(stocks)
                .setPage(0)
                .setSize(10000)
                .setTotalPages(1)
                .setTotalElements(stocks.size())
                .build();

        when(stockService.getAllStocks(0, 10000)).thenReturn(wrapper);

        ResponseEntity<ResponseObject<List<Stock>>> response = stockController.getAllStocks(0, 10000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(stocks, response.getBody().getContent());
        assertEquals("List of all stocks retrieved successfully!", response.getBody().getMessage());
        assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
        verify(stockService).getAllStocks(0, 10000);
    }
}