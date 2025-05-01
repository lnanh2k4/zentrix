package com.zentrix.controller;

import com.zentrix.model.entity.StockDetail;
import com.zentrix.model.request.CreateStockDetailRequest;
import com.zentrix.service.ProductTypeBranchService;
import com.zentrix.service.StockDetailService;
import com.zentrix.service.StockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 *
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 13, 2025
 */
@RestController
@RequestMapping("/api/stock-details")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Tag(name = "StockDetail Controller", description = "This class contains theStockDetail CRUD methods.")
public class StockDetailController {

    StockDetailService stockDetailService;
    StockService stockService;
    ProductTypeBranchService productTypeBranchService;

    /**
     * Creates a new StockDetail.
     *
     * @param request The request object containing stock detail information.
     * @return ResponseEntity containing the created StockDetail.
     */
    @PostMapping("/create")
    @Operation(summary = "Create StockDetail", description = "This method is usedto create a stockDetail")

    public ResponseEntity<StockDetail> createStockDetail(@Valid @RequestBody CreateStockDetailRequest request) {
        StockDetail stockDetail = new StockDetail();
        stockDetail.setStockId(stockService.getStockById(request.getStockId()));
        stockDetail.setProdTypeBrchId(productTypeBranchService.findProductTypeBranchById(request.getProdTypeBrchId()));
        stockDetail.setStockQuantity(request.getStockQuantity());
        stockDetail.setImportPrice(request.getImportPrice());

        return ResponseEntity.ok(stockDetailService.createStockDetail(stockDetail));
    }

    /**
     * Retrieves a StockDetail by its ID.
     *
     * @param id The ID of the StockDetail.
     * @return ResponseEntity containing the requested StockDetail.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get StockDetail By Id", description = "This method isused to get a stockDetail by Id")

    public ResponseEntity<StockDetail> getStockDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(stockDetailService.getStockDetailById(id));
    }

    /**
     * Retrieves all StockDetail entries associated with a specific Stock ID.
     *
     * @param stockId The ID of the Stock.
     * @return ResponseEntity containing a list of StockDetail entries.
     */
    @GetMapping("/stock/{stockId}")
    @Operation(summary = "Get StockDetail by StockId", description = "This methodis used to get all StockDetail by StockID")

    public ResponseEntity<List<StockDetail>> getStockDetailsByStockId(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockDetailService.getStockDetailsByStockId(stockId));
    }

    /**
     * Updates an existing StockDetail by ID.
     *
     * @param id          The ID of the StockDetail to update.
     * @param stockDetail The updated StockDetail object.
     * @return ResponseEntity containing the updated StockDetail.
     */
    @PutMapping("/update/{id}")
    @Operation(summary = "Update StockDetail", description = "This method is usedto updatea stockDetail by Id (Not use)")

    public ResponseEntity<StockDetail> updateStockDetail(@Valid @PathVariable Long id,
            @RequestBody StockDetail stockDetail) {
        return ResponseEntity.ok(stockDetailService.updateStockDetail(id,
                stockDetail));
    }

    /**
     * Deletes a StockDetail by ID.
     *
     * @param id The ID of the StockDetail to delete.
     * @return ResponseEntity containing a boolean value indicating success or
     *         failure.
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete stockDetail", description = "This method is usedto deletea stockDetail  by Id (Not use)")

    public ResponseEntity<Boolean> deleteStockDetail(@PathVariable Long id) {
        return ResponseEntity.ok(stockDetailService.deleteStockDetail(id));
    }
}