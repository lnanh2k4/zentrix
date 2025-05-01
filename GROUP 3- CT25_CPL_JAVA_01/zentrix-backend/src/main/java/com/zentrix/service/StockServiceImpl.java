package com.zentrix.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.zentrix.model.entity.Stock;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.request.CreateStockRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.StaffRepository;
import com.zentrix.repository.StockRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

/*
* @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
* @date February 13, 2025
*/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockServiceImpl implements StockService {

    StockRepository stockRepository;
    BranchService branchService;
    SupplierService supplierService;
    StaffRepository staffRepository;
    UserService userService;

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public Stock createStock(CreateStockRequest request) {
        try {
            Stock stock = new Stock();
            stock.setImportDate(request.getImportDate());
            stock.setBrchId(branchService.getBranchById(request.getBrchId()));
            stock.setSupplierId(supplierService.getSupplierById(request.getSupplierId()));
            stock.setCreatedBy(staffRepository.findStaffByUserId(userService.findUserByUserId(request.getCreatedBy())));
            stock.setCreatedAt(LocalDateTime.now());
            return stockRepository.save(stock);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Stock getStockById(Long id) {
        try {
            return stockRepository.findById(id).get();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public PaginationWrapper<List<Stock>> getAllStocks(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<Stock> stockPage = stockRepository.findAll(pageable);

            return new PaginationWrapper.Builder<List<Stock>>()
                    .setData(stockPage.getContent())
                    .setPaginationInfo(stockPage)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}
