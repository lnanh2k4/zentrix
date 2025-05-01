package com.zentrix.service;

import com.zentrix.model.entity.Branch;
import com.zentrix.model.entity.Staff;
import com.zentrix.model.entity.Stock;
import com.zentrix.model.entity.Supplier;
import com.zentrix.model.entity.User;
import com.zentrix.model.request.CreateStockRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.StaffRepository;
import com.zentrix.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private BranchService branchService;

    @Mock
    private SupplierService supplierService;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private StockServiceImpl stockService;

    @BeforeEach
    void setUp() {
        reset(stockRepository, branchService, supplierService, staffRepository, userService);
    }

    // Test for createStock
    @Test
    void createStock_Success() {
        CreateStockRequest request = new CreateStockRequest();
        request.setImportDate(LocalDate.now());
        request.setBrchId(1L);
        request.setSupplierId(2);
        request.setCreatedBy(3L);

        Branch branch = new Branch();
        Supplier supplier = new Supplier();
        User user = new User();
        Staff staff = new Staff();
        Stock stock = new Stock();
        stock.setImportDate(request.getImportDate());
        stock.setBrchId(branch);
        stock.setSupplierId(supplier);
        stock.setCreatedBy(staff);

        when(branchService.getBranchById(1L)).thenReturn(branch);
        when(supplierService.getSupplierById(2)).thenReturn(supplier);
        when(userService.findUserByUserId(3L)).thenReturn(user);
        when(staffRepository.findStaffByUserId(user)).thenReturn(staff);
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);

        Stock result = stockService.createStock(request);

        assertNotNull(result);
        assertEquals(branch, result.getBrchId());
        assertEquals(supplier, result.getSupplierId());
        assertEquals(staff, result.getCreatedBy());
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    void createStock_Exception_ReturnsNull() {
        CreateStockRequest request = new CreateStockRequest();
        request.setBrchId(1L);

        when(branchService.getBranchById(1L)).thenThrow(new RuntimeException("Branch error"));

        Stock result = stockService.createStock(request);

        assertNull(result);
        verify(branchService).getBranchById(1L);
        verify(stockRepository, never()).save(any(Stock.class));
    }

    // Test for getStockById
    @Test
    void getStockById_Success() {
        Long id = 1L;
        Stock stock = new Stock();

        when(stockRepository.findById(id)).thenReturn(Optional.of(stock));

        Stock result = stockService.getStockById(id);

        assertNotNull(result);
        assertEquals(stock, result);
        verify(stockRepository).findById(id);
    }

    @Test
    void getStockById_NotFound_ReturnsNull() {
        Long id = 1L;

        when(stockRepository.findById(id)).thenReturn(Optional.empty());

        Stock result = stockService.getStockById(id);

        assertNull(result);
        verify(stockRepository).findById(id);
    }

    // Test for getAllStocks
    @Test
    void getAllStocks_Success() {
        int page = 0;
        int size = 10;
        List<Stock> stocks = Arrays.asList(new Stock(), new Stock());
        Page<Stock> stockPage = new PageImpl<>(stocks, PageRequest.of(page, size), stocks.size());

        when(stockRepository.findAll(any(Pageable.class))).thenReturn(stockPage);

        PaginationWrapper<List<Stock>> result = stockService.getAllStocks(page, size);

        assertNotNull(result);
        assertEquals(stocks, result.getData());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(stocks.size(), result.getTotalElements());
        verify(stockRepository).findAll(PageRequest.of(page, size));
    }

    @Test
    void getAllStocks_Exception_ReturnsNull() {
        int page = 0;
        int size = 10;

        when(stockRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("Pagination error"));

        PaginationWrapper<List<Stock>> result = stockService.getAllStocks(page, size);

        assertNull(result);
        verify(stockRepository).findAll(PageRequest.of(page, size));
    }
}