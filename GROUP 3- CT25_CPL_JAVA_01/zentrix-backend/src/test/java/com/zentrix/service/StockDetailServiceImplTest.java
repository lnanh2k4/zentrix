package com.zentrix.service;

import com.zentrix.model.entity.Stock;
import com.zentrix.model.entity.StockDetail;
import com.zentrix.repository.StockDetailRepository;
import com.zentrix.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockDetailServiceImplTest {

    @Mock
    private StockDetailRepository stockDetailRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockDetailServiceImpl stockDetailService;

    @BeforeEach
    void setUp() {
        reset(stockDetailRepository, stockRepository);
    }

    // Test for createStockDetail
    @Test
    void createStockDetail_Success() {
        StockDetail stockDetail = new StockDetail();

        when(stockDetailRepository.save(any(StockDetail.class))).thenReturn(stockDetail);

        StockDetail result = stockDetailService.createStockDetail(stockDetail);

        assertNotNull(result);
        assertEquals(stockDetail, result);
        verify(stockDetailRepository).save(stockDetail);
    }

    @Test
    void createStockDetail_Exception_ReturnsNull() {
        StockDetail stockDetail = new StockDetail();

        when(stockDetailRepository.save(any(StockDetail.class))).thenThrow(new RuntimeException("Save error"));

        StockDetail result = stockDetailService.createStockDetail(stockDetail);

        assertNull(result);
        verify(stockDetailRepository).save(stockDetail);
    }

    // Test for getStockDetailById
    @Test
    void getStockDetailById_Success() {
        Long id = 1L;
        StockDetail stockDetail = new StockDetail();

        when(stockDetailRepository.findById(id)).thenReturn(Optional.of(stockDetail));

        StockDetail result = stockDetailService.getStockDetailById(id);

        assertNotNull(result);
        assertEquals(stockDetail, result);
        verify(stockDetailRepository).findById(id);
    }

    @Test
    void getStockDetailById_NotFound_ReturnsNull() {
        Long id = 1L;

        when(stockDetailRepository.findById(id)).thenReturn(Optional.empty());

        StockDetail result = stockDetailService.getStockDetailById(id);

        assertNull(result);
        verify(stockDetailRepository).findById(id);
    }

    // Test for getStockDetailsByStockId
    @Test
    void getStockDetailsByStockId_Success() {
        Long stockId = 1L;
        Stock stock = new Stock();
        List<StockDetail> stockDetails = Arrays.asList(new StockDetail());

        when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(stockDetailRepository.findByStockId(stock)).thenReturn(stockDetails);

        List<StockDetail> result = stockDetailService.getStockDetailsByStockId(stockId);

        assertNotNull(result);
        assertEquals(stockDetails, result);
        verify(stockRepository).findById(stockId);
        verify(stockDetailRepository).findByStockId(stock);
    }

    @Test
    void getStockDetailsByStockId_StockNotFound_ReturnsNull() {
        Long stockId = 1L;

        when(stockRepository.findById(stockId)).thenReturn(Optional.empty());

        List<StockDetail> result = stockDetailService.getStockDetailsByStockId(stockId);

        assertNull(result);
        verify(stockRepository).findById(stockId);
        verify(stockDetailRepository, never()).findByStockId(any());
    }

    // Test for updateStockDetail
    @Test
    void updateStockDetail_Success() {
        Long id = 1L;
        StockDetail stockDetail = new StockDetail();
        stockDetail.setStockQuantity(10);
        stockDetail.setImportPrice(100.0);

        StockDetail existingDetail = new StockDetail();

        when(stockDetailRepository.findById(id)).thenReturn(Optional.of(existingDetail));
        when(stockDetailRepository.save(any(StockDetail.class))).thenReturn(existingDetail);

        StockDetail result = stockDetailService.updateStockDetail(id, stockDetail);

        assertNotNull(result);
        assertEquals(10, result.getStockQuantity());
        assertEquals(100.0, result.getImportPrice());
        verify(stockDetailRepository).findById(id);
        verify(stockDetailRepository).save(existingDetail);
    }

    @Test
    void updateStockDetail_NotFound_ReturnsNull() {
        Long id = 1L;
        StockDetail stockDetail = new StockDetail();

        when(stockDetailRepository.findById(id)).thenReturn(Optional.empty());

        StockDetail result = stockDetailService.updateStockDetail(id, stockDetail);

        assertNull(result);
        verify(stockDetailRepository).findById(id);
        verify(stockDetailRepository, never()).save(any(StockDetail.class));
    }

    // Test for deleteStockDetail
    @Test
    void deleteStockDetail_Success() {
        Long id = 1L;

        when(stockDetailRepository.existsById(id)).thenReturn(true);
        doNothing().when(stockDetailRepository).deleteById(id);

        boolean result = stockDetailService.deleteStockDetail(id);

        assertTrue(result);
        verify(stockDetailRepository).existsById(id);
        verify(stockDetailRepository).deleteById(id);
    }

    @Test
    void deleteStockDetail_NotFound_ReturnsFalse() {
        Long id = 1L;

        when(stockDetailRepository.existsById(id)).thenReturn(false);

        boolean result = stockDetailService.deleteStockDetail(id);

        assertFalse(result);
        verify(stockDetailRepository).existsById(id);
        verify(stockDetailRepository, never()).deleteById(id);
    }

    @Test
    void deleteStockDetail_Exception_ReturnsFalse() {
        Long id = 1L;

        when(stockDetailRepository.existsById(id)).thenReturn(true);
        doThrow(new RuntimeException("Delete error")).when(stockDetailRepository).deleteById(id);

        boolean result = stockDetailService.deleteStockDetail(id);

        assertFalse(result);
        verify(stockDetailRepository).existsById(id);
        verify(stockDetailRepository).deleteById(id);
    }
}