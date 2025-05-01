package com.zentrix.service;

import com.zentrix.model.entity.Branch;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.ProductTypeBranch;
import com.zentrix.model.request.ProductTypeBranchRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.ProductTypeBranchRepository;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductTypeBranchServiceImplTest {

    @Mock
    private ProductTypeBranchRepository productTypeBranchRepository;

    @Mock
    private ProductTypeService productTypeService;

    @Mock
    private BranchService branchService;

    @InjectMocks
    private ProductTypeBranchServiceImpl productTypeBranchService;

    @BeforeEach
    void setUp() {
        reset(productTypeBranchRepository, productTypeService, branchService);
    }

    // Test for saveProductTypeBranch
    @Test
    void saveProductTypeBranch_NewEntry_Success() {
        ProductTypeBranchRequest request = new ProductTypeBranchRequest();
        request.setProdTypeId(1L);
        request.setBrchId(2L);
        request.setQuantity(10);

        ProductType productType = new ProductType();
        Branch branch = new Branch();
        ProductTypeBranch productTypeBranch = new ProductTypeBranch();
        productTypeBranch.setProdTypeId(productType);
        productTypeBranch.setBrchId(branch);
        productTypeBranch.setQuantity(10);
        productTypeBranch.setStatus(1);

        when(productTypeService.findProductTypeById(1L)).thenReturn(productType);
        when(branchService.getBranchById(2L)).thenReturn(branch);
        when(productTypeBranchRepository.findByProdTypeIdAndBrchId(productType, branch)).thenReturn(null);
        when(productTypeBranchRepository.save(any(ProductTypeBranch.class))).thenReturn(productTypeBranch);

        ProductTypeBranch result = productTypeBranchService.saveProductTypeBranch(request);

        assertNotNull(result);
        assertEquals(productType, result.getProdTypeId());
        assertEquals(branch, result.getBrchId());
        assertEquals(10, result.getQuantity());
        assertEquals(1, result.getStatus());
        verify(productTypeService).findProductTypeById(1L);
        verify(branchService).getBranchById(2L);
        verify(productTypeBranchRepository).findByProdTypeIdAndBrchId(productType, branch);
        verify(productTypeBranchRepository).save(any(ProductTypeBranch.class));
    }

    @Test
    void saveProductTypeBranch_ExistingEntry_UpdatesQuantity() {
        ProductTypeBranchRequest request = new ProductTypeBranchRequest();
        request.setProdTypeId(1L);
        request.setBrchId(2L);
        request.setQuantity(5);

        ProductType productType = new ProductType();
        Branch branch = new Branch();
        ProductTypeBranch existingProductTypeBranch = new ProductTypeBranch();
        existingProductTypeBranch.setQuantity(10);

        when(productTypeService.findProductTypeById(1L)).thenReturn(productType);
        when(branchService.getBranchById(2L)).thenReturn(branch);
        when(productTypeBranchRepository.findByProdTypeIdAndBrchId(productType, branch))
                .thenReturn(existingProductTypeBranch);
        when(productTypeBranchRepository.save(any(ProductTypeBranch.class))).thenReturn(existingProductTypeBranch);

        ProductTypeBranch result = productTypeBranchService.saveProductTypeBranch(request);

        assertNotNull(result);
        assertEquals(15, result.getQuantity()); // 10 + 5
        verify(productTypeBranchRepository).save(existingProductTypeBranch);
    }

    @Test
    void saveProductTypeBranch_ProductTypeNotFound_ReturnsNull() {
        ProductTypeBranchRequest request = new ProductTypeBranchRequest();
        request.setProdTypeId(1L);
        request.setBrchId(2L);

        when(productTypeService.findProductTypeById(1L)).thenReturn(null);

        ProductTypeBranch result = productTypeBranchService.saveProductTypeBranch(request);

        assertNull(result);
        verify(productTypeService).findProductTypeById(1L);
        verify(branchService, never()).getBranchById(anyLong());
    }

    // Test for findProductTypeBranchById
    @Test
    void findProductTypeBranchById_Success() {
        Long id = 1L;
        ProductTypeBranch productTypeBranch = new ProductTypeBranch();

        when(productTypeBranchRepository.findById(id)).thenReturn(Optional.of(productTypeBranch));

        ProductTypeBranch result = productTypeBranchService.findProductTypeBranchById(id);

        assertNotNull(result);
        assertEquals(productTypeBranch, result);
        verify(productTypeBranchRepository).findById(id);
    }

    @Test
    void findProductTypeBranchById_NotFound_ReturnsNull() {
        Long id = 1L;

        when(productTypeBranchRepository.findById(id)).thenReturn(Optional.empty());

        ProductTypeBranch result = productTypeBranchService.findProductTypeBranchById(id);

        assertNull(result);
        verify(productTypeBranchRepository).findById(id);
    }

    // Test for getAllProductTypeBranches
    @Test
    void getAllProductTypeBranches_Success() {
        int page = 0;
        int size = 10;
        List<ProductTypeBranch> branches = Arrays.asList(new ProductTypeBranch(), new ProductTypeBranch());
        Page<ProductTypeBranch> branchPage = new PageImpl<>(branches, PageRequest.of(page, size), branches.size());

        when(productTypeBranchRepository.findAll(any(Pageable.class))).thenReturn(branchPage);

        PaginationWrapper<List<ProductTypeBranch>> result = productTypeBranchService.getAllProductTypeBranches(page,
                size);

        assertNotNull(result);
        assertEquals(branches, result.getData());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(branches.size(), result.getTotalElements());
        verify(productTypeBranchRepository).findAll(PageRequest.of(page, size));
    }

    // Test for deleteProductTypeBranch
    @Test
    void deleteProductTypeBranch_Success() {
        Long id = 1L;

        when(productTypeBranchRepository.existsById(id)).thenReturn(true);
        doNothing().when(productTypeBranchRepository).deleteById(id);

        boolean result = productTypeBranchService.deleteProductTypeBranch(id);

        assertTrue(result);
        verify(productTypeBranchRepository).existsById(id);
        verify(productTypeBranchRepository).deleteById(id);
    }

    @Test
    void deleteProductTypeBranch_NotFound_ReturnsFalse() {
        Long id = 1L;

        when(productTypeBranchRepository.existsById(id)).thenReturn(false);

        boolean result = productTypeBranchService.deleteProductTypeBranch(id);

        assertFalse(result);
        verify(productTypeBranchRepository).existsById(id);
        verify(productTypeBranchRepository, never()).deleteById(id);
    }

    // Test for updateProductTypeBranch
    @Test
    void updateProductTypeBranch_Success() {
        Long id = 1L;
        ProductTypeBranchRequest request = new ProductTypeBranchRequest();
        request.setProdTypeId(1L);
        request.setBrchId(2L);
        request.setQuantity(20);

        ProductTypeBranch existingBranch = new ProductTypeBranch();
        ProductType productType = new ProductType();
        Branch branch = new Branch();

        when(productTypeBranchRepository.findById(id)).thenReturn(Optional.of(existingBranch));
        when(productTypeService.findProductTypeById(1L)).thenReturn(productType);
        when(branchService.getBranchById(2L)).thenReturn(branch);
        when(productTypeBranchRepository.save(any(ProductTypeBranch.class))).thenReturn(existingBranch);

        ProductTypeBranch result = productTypeBranchService.updateProductTypeBranch(id, request);

        assertNotNull(result);
        assertEquals(productType, result.getProdTypeId());
        assertEquals(branch, result.getBrchId());
        assertEquals(20, result.getQuantity());
        verify(productTypeBranchRepository).save(existingBranch);
    }

    @Test
    void updateProductTypeBranch_NotFound_ReturnsNull() {
        Long id = 1L;
        ProductTypeBranchRequest request = new ProductTypeBranchRequest();

        when(productTypeBranchRepository.findById(id)).thenReturn(Optional.empty());

        ProductTypeBranch result = productTypeBranchService.updateProductTypeBranch(id, request);

        assertNull(result);
        verify(productTypeBranchRepository).findById(id);
        verify(productTypeService, never()).findProductTypeById(anyLong());
    }

    // Test for findByProdTypeId
    @Test
    void findByProdTypeId_Success() {
        ProductType productType = new ProductType();
        List<ProductTypeBranch> branches = Arrays.asList(new ProductTypeBranch());

        when(productTypeBranchRepository.findByProdTypeId(productType)).thenReturn(branches);

        List<ProductTypeBranch> result = productTypeBranchService.findByProdTypeId(productType);

        assertEquals(branches, result);
        verify(productTypeBranchRepository).findByProdTypeId(productType);
    }

    @Test
    void findByProdTypeId_NullInput_ReturnsEmptyList() {
        List<ProductTypeBranch> result = productTypeBranchService.findByProdTypeId(null);

        assertEquals(Collections.emptyList(), result);
        verify(productTypeBranchRepository, never()).findByProdTypeId(any());
    }

    // Test for getProductTypeBranchByProdTypeId
    @Test
    void getProductTypeBranchByProdTypeId_Success() {
        Long id = 1L;
        List<ProductTypeBranch> branches = Arrays.asList(new ProductTypeBranch());

        when(productTypeBranchRepository.findByProdTypeIdProdTypeId(id)).thenReturn(branches);

        List<ProductTypeBranch> result = productTypeBranchService.getProductTypeBranchByProdTypeId(id);

        assertEquals(branches, result);
        verify(productTypeBranchRepository).findByProdTypeIdProdTypeId(id);
    }

    @Test
    void getProductTypeBranchByProdTypeId_NullInput_ReturnsEmptyList() {
        List<ProductTypeBranch> result = productTypeBranchService.getProductTypeBranchByProdTypeId(null);

        assertEquals(Collections.emptyList(), result);
        verify(productTypeBranchRepository, never()).findByProdTypeIdProdTypeId(anyLong());
    }

    // Test for findByProdTypeIdAndBrchId
    @Test
    void findByProdTypeIdAndBrchId_Success() {
        Long prodTypeId = 1L;
        Long brchId = 2L;
        ProductTypeBranch productTypeBranch = new ProductTypeBranch();

        when(productTypeBranchRepository.findByProdTypeIdProdTypeIdAndBrchIdBrchId(prodTypeId, brchId))
                .thenReturn(productTypeBranch);

        ProductTypeBranch result = productTypeBranchService.findByProdTypeIdAndBrchId(prodTypeId, brchId);

        assertEquals(productTypeBranch, result);
        verify(productTypeBranchRepository).findByProdTypeIdProdTypeIdAndBrchIdBrchId(prodTypeId, brchId);
    }

    @Test
    void findByProdTypeIdAndBrchId_NullInput_ReturnsNull() {
        ProductTypeBranch result = productTypeBranchService.findByProdTypeIdAndBrchId(null, 2L);
        assertNull(result);

        result = productTypeBranchService.findByProdTypeIdAndBrchId(1L, null);
        assertNull(result);

        verify(productTypeBranchRepository, never()).findByProdTypeIdProdTypeIdAndBrchIdBrchId(anyLong(), anyLong());
    }
}