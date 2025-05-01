package com.zentrix.service;

import com.zentrix.model.entity.Product;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.request.ProductTypeRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.ProductTypeRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductTypeServiceImplTest {

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductTypeServiceImpl productTypeService;

    @BeforeEach
    void setUp() {
        reset(productTypeRepository, productService);
    }

    // Test for saveProductType
    @Test
    void saveProductType_Success() {
        ProductTypeRequest request = new ProductTypeRequest();
        request.setProdId(1L);
        request.setProdTypeName("TypeName");
        request.setProdTypePrice(100.0);
        request.setUnit("Unit");
        request.setUnitPrice(10.0);

        Product product = new Product();
        ProductType productType = new ProductType();
        productType.setProdId(product);
        productType.setProdTypeName("TypeName");
        productType.setProdTypePrice(100.0);
        productType.setUnit("Unit");
        productType.setUnitPrice(10.0);
        productType.setStatus(1);

        when(productService.findProductById(1L)).thenReturn(product);
        when(productTypeRepository.save(any(ProductType.class))).thenReturn(productType);

        ProductType result = productTypeService.saveProductType(request);

        assertNotNull(result);
        assertEquals(product, result.getProdId());
        assertEquals("TypeName", result.getProdTypeName());
        assertEquals(100.0, result.getProdTypePrice());
        assertEquals("Unit", result.getUnit());
        assertEquals(10.0, result.getUnitPrice());
        assertEquals(1, result.getStatus());
        verify(productService).findProductById(1L);
        verify(productTypeRepository).save(any(ProductType.class));
    }

    @Test
    void saveProductType_ProductNotFound_ThrowsIllegalArgumentException() {
        ProductTypeRequest request = new ProductTypeRequest();
        request.setProdId(1L);

        when(productService.findProductById(1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productTypeService.saveProductType(request);
        });

        assertEquals("Product not found for ID: 1", exception.getMessage());
        verify(productService).findProductById(1L);
        verify(productTypeRepository, never()).save(any(ProductType.class));
    }

    // Test for updateProductType
    @Test
    void updateProductType_Success() {
        Long id = 1L;
        ProductTypeRequest request = new ProductTypeRequest();
        request.setProdId(2L);
        request.setProdTypeName("UpdatedName");
        request.setProdTypePrice(200.0);
        request.setUnit("UpdatedUnit");
        request.setUnitPrice(20.0);

        ProductType existingProductType = new ProductType();
        Product product = new Product();

        when(productTypeRepository.findById(id)).thenReturn(Optional.of(existingProductType));
        when(productService.findProductById(2L)).thenReturn(product);
        when(productTypeRepository.save(any(ProductType.class))).thenReturn(existingProductType);

        ProductType result = productTypeService.updateProductType(id, request);

        assertNotNull(result);
        assertEquals(product, result.getProdId());
        assertEquals("UpdatedName", result.getProdTypeName());
        assertEquals(200.0, result.getProdTypePrice());
        assertEquals("UpdatedUnit", result.getUnit());
        assertEquals(20.0, result.getUnitPrice());
        verify(productTypeRepository).save(existingProductType);
    }

    @Test
    void updateProductType_NotFound_ThrowsIllegalArgumentException() {
        Long id = 1L;
        ProductTypeRequest request = new ProductTypeRequest();

        when(productTypeRepository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productTypeService.updateProductType(id, request);
        });

        assertEquals("ProductType not found for ID: 1", exception.getMessage());
        verify(productTypeRepository).findById(id);
        verify(productService, never()).findProductById(anyLong());
    }

    // Test for findProductTypeById
    @Test
    void findProductTypeById_Success() {
        Long id = 1L;
        ProductType productType = new ProductType();

        when(productTypeRepository.findById(id)).thenReturn(Optional.of(productType));

        ProductType result = productTypeService.findProductTypeById(id);

        assertNotNull(result);
        assertEquals(productType, result);
        verify(productTypeRepository).findById(id);
    }

    @Test
    void findProductTypeById_NotFound_ReturnsNull() {
        Long id = 1L;

        when(productTypeRepository.findById(id)).thenReturn(Optional.empty());

        ProductType result = productTypeService.findProductTypeById(id);

        assertNull(result);
        verify(productTypeRepository).findById(id);
    }

    // Test for getAllProductTypes
    @Test
    void getAllProductTypes_Success() {
        int page = 0;
        int size = 10;
        List<ProductType> productTypes = Arrays.asList(new ProductType(), new ProductType());
        Page<ProductType> productTypePage = new PageImpl<>(productTypes, PageRequest.of(page, size),
                productTypes.size());

        when(productTypeRepository.findAll(any(Pageable.class))).thenReturn(productTypePage);

        PaginationWrapper<List<ProductType>> result = productTypeService.getAllProductTypes(page, size);

        assertNotNull(result);
        assertEquals(productTypes, result.getData());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(productTypes.size(), result.getTotalElements());
        verify(productTypeRepository).findAll(PageRequest.of(page, size));
    }

    // Test for deleteProductType
    @Test
    void deleteProductType_Success() {
        Long id = 1L;

        doNothing().when(productTypeRepository).deleteById(id);

        boolean result = productTypeService.deleteProductType(id);

        assertTrue(result);
        verify(productTypeRepository).deleteById(id);
    }

    @Test
    void deleteProductType_ThrowsRuntimeException() {
        Long id = 1L;

        doThrow(new RuntimeException("Database error")).when(productTypeRepository).deleteById(id);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productTypeService.deleteProductType(id);
        });

        assertEquals("Failed to delete ProductType: Database error", exception.getMessage());
        verify(productTypeRepository).deleteById(id);
    }

    // Test for findProductTypesByProductId
    @Test
    void findProductTypesByProductId_Success() {
        Long prodId = 1L;
        Product product = new Product();
        List<ProductType> productTypes = Arrays.asList(new ProductType());

        when(productService.findProductById(prodId)).thenReturn(product);
        when(productTypeRepository.findByProdId(product)).thenReturn(productTypes);

        List<ProductType> result = productTypeService.findProductTypesByProductId(prodId);

        assertEquals(productTypes, result);
        verify(productService).findProductById(prodId);
        verify(productTypeRepository).findByProdId(product);
    }

    @Test
    void findProductTypesByProductId_ProductNotFound_ThrowsIllegalArgumentException() {
        Long prodId = 1L;

        when(productService.findProductById(prodId)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productTypeService.findProductTypesByProductId(prodId);
        });

        assertEquals("Failed to retrieve ProductTypes for Product ID 1: Product not found for ID: 1",
                exception.getMessage());
        verify(productService).findProductById(prodId);
        verify(productTypeRepository, never()).findByProdId(any());
    }

    // Test for unactiveProductType
    @Test
    void unactiveProductType_ActiveToInactive_Success() {
        Long id = 1L;
        ProductType productType = new ProductType();
        productType.setStatus(1);

        when(productTypeRepository.findById(id)).thenReturn(Optional.of(productType));
        when(productTypeRepository.save(any(ProductType.class))).thenReturn(productType);

        boolean result = productTypeService.unactiveProductType(id, 1);

        assertTrue(result);
        assertEquals(0, productType.getStatus());
        verify(productTypeRepository).findById(id);
        verify(productTypeRepository).save(productType);
    }

    @Test
    void unactiveProductType_InactiveToActive_Success() {
        Long id = 1L;
        ProductType productType = new ProductType();
        productType.setStatus(0);

        when(productTypeRepository.findById(id)).thenReturn(Optional.of(productType));
        when(productTypeRepository.save(any(ProductType.class))).thenReturn(productType);

        boolean result = productTypeService.unactiveProductType(id, 1);

        assertTrue(result);
        assertEquals(1, productType.getStatus());
        verify(productTypeRepository).findById(id);
        verify(productTypeRepository).save(productType);
    }

    @Test
    void unactiveProductType_NotFound_ReturnsFalse() {
        Long id = 1L;

        when(productTypeRepository.findById(id)).thenReturn(Optional.empty());

        boolean result = productTypeService.unactiveProductType(id, 1);

        assertFalse(result);
        verify(productTypeRepository).findById(id);
        verify(productTypeRepository, never()).save(any(ProductType.class));
    }

    // Test for searchProductTypesByName
    @Test
    void searchProductTypesByName_Success() {
        String keyword = "Type";
        int page = 0;
        int size = 10;
        List<ProductType> productTypes = Arrays.asList(new ProductType(), new ProductType());
        Page<ProductType> productTypePage = new PageImpl<>(productTypes, PageRequest.of(page, size),
                productTypes.size());

        when(productTypeRepository.findByProdTypeNameContainingIgnoreCase(eq("Type"), any(Pageable.class)))
                .thenReturn(productTypePage);

        PaginationWrapper<List<ProductType>> result = productTypeService.searchProductTypesByName(keyword, page, size);

        assertNotNull(result);
        assertEquals(productTypes, result.getData());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(productTypes.size(), result.getTotalElements());
        verify(productTypeRepository).findByProdTypeNameContainingIgnoreCase("Type", PageRequest.of(page, size));
    }

    @Test
    void searchProductTypesByName_EmptyKeyword_ThrowsIllegalArgumentException() {
        String keyword = "";

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productTypeService.searchProductTypesByName(keyword, 0, 10);
        });

        assertEquals("Failed to search ProductTypes by name: Search keyword cannot be empty", exception.getMessage());
        verify(productTypeRepository, never()).findByProdTypeNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }
}