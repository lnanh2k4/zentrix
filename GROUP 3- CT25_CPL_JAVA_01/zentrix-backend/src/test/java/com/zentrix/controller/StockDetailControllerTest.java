package com.zentrix.controller;

import com.zentrix.model.entity.ProductTypeBranch;
import com.zentrix.model.entity.Stock;
import com.zentrix.model.entity.StockDetail;
import com.zentrix.model.request.CreateStockDetailRequest;
import com.zentrix.service.ProductTypeBranchService;
import com.zentrix.service.StockDetailService;
import com.zentrix.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockDetailControllerTest {

    @Mock
    private StockDetailService stockDetailService;
    @Mock
    private StockService stockService;
    @Mock
    private ProductTypeBranchService productTypeBranchService;

    @InjectMocks
    private StockDetailController stockDetailController;

    @BeforeEach
    void setUp() {
        reset(stockDetailService, stockService, productTypeBranchService);
    }

    // Test for createStockDetail
    @Test
    void createStockDetail_Success() {
        CreateStockDetailRequest request = new CreateStockDetailRequest();
        request.setStockId(1L);
        request.setProdTypeBrchId(2L);
        request.setStockQuantity(100);
        request.setImportPrice(50.0);

        Stock stock = new Stock();
        ProductTypeBranch productTypeBranch = new ProductTypeBranch();
        StockDetail stockDetail = new StockDetail();
        stockDetail.setStockId(stock);
        stockDetail.setProdTypeBrchId(productTypeBranch);
        stockDetail.setStockQuantity(100);
        stockDetail.setImportPrice(50.0);

        when(stockService.getStockById(1L)).thenReturn(stock);
        when(productTypeBranchService.findProductTypeBranchById(2L)).thenReturn(productTypeBranch);
        when(stockDetailService.createStockDetail(any(StockDetail.class))).thenReturn(stockDetail);

        ResponseEntity<StockDetail> response = stockDetailController.createStockDetail(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(stockDetail, response.getBody());
        assertEquals(stock, response.getBody().getStockId());
        assertEquals(productTypeBranch, response.getBody().getProdTypeBrchId());
        assertEquals(100, response.getBody().getStockQuantity());
        assertEquals(50.0, response.getBody().getImportPrice());
        verify(stockService).getStockById(1L);
        verify(productTypeBranchService).findProductTypeBranchById(2L);
        verify(stockDetailService).createStockDetail(any(StockDetail.class));
    }

    // Test for getStockDetailById
    @Test
    void getStockDetailById_Success() {
        Long id = 1L;
        StockDetail stockDetail = new StockDetail();

        when(stockDetailService.getStockDetailById(id)).thenReturn(stockDetail);

        ResponseEntity<StockDetail> response = stockDetailController.getStockDetailById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(stockDetail, response.getBody());
        verify(stockDetailService).getStockDetailById(id);
    }

    // Test for getStockDetailsByStockId
    @Test
    void getStockDetailsByStockId_Success() {
        Long stockId = 1L;
        List<StockDetail> stockDetails = Arrays.asList(new StockDetail());

        when(stockDetailService.getStockDetailsByStockId(stockId)).thenReturn(stockDetails);

        ResponseEntity<List<StockDetail>> response = stockDetailController.getStockDetailsByStockId(stockId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(stockDetails, response.getBody());
        verify(stockDetailService).getStockDetailsByStockId(stockId);
    }

    // Test for updateStockDetail
    @Test
    void updateStockDetail_Success() {
        Long id = 1L;
        StockDetail stockDetail = new StockDetail();
        StockDetail updatedStockDetail = new StockDetail();

        when(stockDetailService.updateStockDetail(id, stockDetail)).thenReturn(updatedStockDetail);

        ResponseEntity<StockDetail> response = stockDetailController.updateStockDetail(id, stockDetail);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedStockDetail, response.getBody());
        verify(stockDetailService).updateStockDetail(id, stockDetail);
    }

    // Test for deleteStockDetail
    @Test
    void deleteStockDetail_Success() {
        Long id = 1L;

        when(stockDetailService.deleteStockDetail(id)).thenReturn(true);

        ResponseEntity<Boolean> response = stockDetailController.deleteStockDetail(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(stockDetailService).deleteStockDetail(id);
    }

    @Test
    void deleteStockDetail_Failure() {
        Long id = 1L;

        when(stockDetailService.deleteStockDetail(id)).thenReturn(false);

        ResponseEntity<Boolean> response = stockDetailController.deleteStockDetail(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
        verify(stockDetailService).deleteStockDetail(id);
    }
}