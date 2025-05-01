package com.zentrix.service;

import com.zentrix.model.entity.Stock;
import com.zentrix.model.entity.StockDetail;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.repository.StockDetailRepository;
import com.zentrix.repository.StockRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/*
* @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
* @date February 13, 2025
*/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockDetailServiceImpl implements StockDetailService {

    StockDetailRepository stockDetailRepository;

    StockRepository stockRepository;

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public StockDetail createStockDetail(StockDetail stockDetail) {
        try {
            return stockDetailRepository.save(stockDetail);
        } catch (Exception e) {

            return null;
        }
    }

    @Override
    public StockDetail getStockDetailById(Long id) {
        try {
            return stockDetailRepository.findById(id).get();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<StockDetail> getStockDetailsByStockId(Long stockId) {
        try {
            Stock stock = stockRepository.findById(stockId).get();

            return stockDetailRepository.findByStockId(stock);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public StockDetail updateStockDetail(Long id, StockDetail stockDetail) {
        try {
            Optional<StockDetail> existingDetail = stockDetailRepository.findById(id);

            if (!existingDetail.isPresent()) {
                return null;
            }

            StockDetail updatedDetail = existingDetail.get();
            updatedDetail.setProdTypeBrchId(stockDetail.getProdTypeBrchId());
            updatedDetail.setStockQuantity(stockDetail.getStockQuantity());
            updatedDetail.setImportPrice(stockDetail.getImportPrice());

            return stockDetailRepository.save(updatedDetail);

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public boolean deleteStockDetail(Long id) {
        try {
            if (!stockDetailRepository.existsById(id)) {
                return false;
            }
            stockDetailRepository.deleteById(id);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
