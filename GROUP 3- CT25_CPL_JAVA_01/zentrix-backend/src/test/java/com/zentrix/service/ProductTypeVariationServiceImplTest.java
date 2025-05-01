package com.zentrix.service;

import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.ProductTypeVariation;
import com.zentrix.model.entity.Variation;
import com.zentrix.model.request.ProductTypeVariationRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.ProductTypeVariationRepository;
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
class ProductTypeVariationServiceImplTest {

    @Mock
    private ProductTypeVariationRepository repository;

    @Mock
    private ProductTypeService productTypeService;

    @Mock
    private VariationService variationService;

    @InjectMocks
    private ProductTypeVariationServiceImpl productTypeVariationService;

    @BeforeEach
    void набор() {
        reset(repository, productTypeService, variationService);
    }

    // Test for isVariationUsed
    @Test
    void isVariationUsed_VariationExists_ReturnsTrue() {
        Long variId = 1L;
        Variation variation = new Variation();
        variation.setVariId(variId);

        when(repository.existsByVariId(variation)).thenReturn(true);

        boolean result = productTypeVariationService.isVariationUsed(variId);

        assertTrue(result);
        verify(repository).existsByVariId(variation);
    }

    @Test
    void isVariationUsed_VariationNotExists_ReturnsFalse() {
        Long variId = 1L;
        Variation variation = new Variation();
        variation.setVariId(variId);

        when(repository.existsByVariId(variation)).thenReturn(false);

        boolean result = productTypeVariationService.isVariationUsed(variId);

        assertFalse(result);
        verify(repository).existsByVariId(variation);
    }

    // Test for getAll
    @Test
    void getAll_Success() {
        int page = 0;
        int size = 10;
        List<ProductTypeVariation> variations = Arrays.asList(new ProductTypeVariation(), new ProductTypeVariation());
        Page<ProductTypeVariation> variationPage = new PageImpl<>(variations, PageRequest.of(page, size),
                variations.size());

        when(repository.findAll(any(Pageable.class))).thenReturn(variationPage);

        PaginationWrapper<List<ProductTypeVariation>> result = productTypeVariationService.getAll(page, size);

        assertNotNull(result);
        assertEquals(variations, result.getData());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(variations.size(), result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(repository).findAll(PageRequest.of(page, size));
    }

    // Test for getById
    @Test
    void getById_Success() {
        Long id = 1L;
        ProductTypeVariation productTypeVariation = new ProductTypeVariation();

        when(repository.findById(id)).thenReturn(Optional.of(productTypeVariation));

        ProductTypeVariation result = productTypeVariationService.getById(id);

        assertNotNull(result);
        assertEquals(productTypeVariation, result);
        verify(repository).findById(id);
    }

    @Test
    void getById_NotFound_ReturnsNull() {
        Long id = 1L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        ProductTypeVariation result = productTypeVariationService.getById(id);

        assertNull(result);
        verify(repository).findById(id);
    }

    // Test for create
    @Test
    void create_Success_DefaultVariNull() {
        ProductTypeVariationRequest request = new ProductTypeVariationRequest();
        request.setProdTypeId(1L);
        request.setVariId(2L);
        request.setProdTypeValue("Value");
        request.setDefaultVari(null);

        ProductType productType = new ProductType();
        Variation variation = new Variation();
        ProductTypeVariation productTypeVariation = new ProductTypeVariation();
        productTypeVariation.setProdTypeId(productType);
        productTypeVariation.setVariId(variation);
        productTypeVariation.setProdTypeValue("Value");
        productTypeVariation.setDefaultVari(0);

        when(productTypeService.findProductTypeById(1L)).thenReturn(productType);
        when(variationService.getById(2L)).thenReturn(variation);
        when(repository.save(any(ProductTypeVariation.class))).thenReturn(productTypeVariation);

        ProductTypeVariation result = productTypeVariationService.create(request);

        assertNotNull(result);
        assertEquals(productType, result.getProdTypeId());
        assertEquals(variation, result.getVariId());
        assertEquals("Value", result.getProdTypeValue());
        assertEquals(0, result.getDefaultVari());
        verify(productTypeService).findProductTypeById(1L);
        verify(variationService).getById(2L);
        verify(repository).save(any(ProductTypeVariation.class));
    }

    @Test
    void create_Success_DefaultVariNotNull() {
        ProductTypeVariationRequest request = new ProductTypeVariationRequest();
        request.setProdTypeId(1L);
        request.setVariId(2L);
        request.setProdTypeValue("Value");
        request.setDefaultVari(1);

        ProductType productType = new ProductType();
        Variation variation = new Variation();
        ProductTypeVariation productTypeVariation = new ProductTypeVariation();
        productTypeVariation.setProdTypeId(productType);
        productTypeVariation.setVariId(variation);
        productTypeVariation.setProdTypeValue("Value");
        productTypeVariation.setDefaultVari(1);

        when(productTypeService.findProductTypeById(1L)).thenReturn(productType);
        when(variationService.getById(2L)).thenReturn(variation);
        when(repository.save(any(ProductTypeVariation.class))).thenReturn(productTypeVariation);

        ProductTypeVariation result = productTypeVariationService.create(request);

        assertNotNull(result);
        assertEquals(productType, result.getProdTypeId());
        assertEquals(variation, result.getVariId());
        assertEquals("Value", result.getProdTypeValue());
        assertEquals(1, result.getDefaultVari());
        verify(productTypeService).findProductTypeById(1L);
        verify(variationService).getById(2L);
        verify(repository).save(any(ProductTypeVariation.class));
    }

    @Test
    void create_ProductTypeNotFound_ReturnsNull() {
        ProductTypeVariationRequest request = new ProductTypeVariationRequest();
        request.setProdTypeId(1L);
        request.setVariId(2L);

        when(productTypeService.findProductTypeById(1L)).thenReturn(null);

        ProductTypeVariation result = productTypeVariationService.create(request);

        assertNull(result);
        verify(productTypeService).findProductTypeById(1L);
        verify(variationService, never()).getById(anyLong());
        verify(repository, never()).save(any(ProductTypeVariation.class));
    }

    @Test
    void create_VariationNotFound_ReturnsNull() {
        ProductTypeVariationRequest request = new ProductTypeVariationRequest();
        request.setProdTypeId(1L);
        request.setVariId(2L);

        ProductType productType = new ProductType();

        when(productTypeService.findProductTypeById(1L)).thenReturn(productType);
        when(variationService.getById(2L)).thenReturn(null);

        ProductTypeVariation result = productTypeVariationService.create(request);

        assertNull(result);
        verify(productTypeService).findProductTypeById(1L);
        verify(variationService).getById(2L);
        verify(repository, never()).save(any(ProductTypeVariation.class));
    }

    // Test for update
    @Test
    void update_Success() {
        Long id = 1L;
        ProductTypeVariationRequest request = new ProductTypeVariationRequest();
        request.setProdTypeId(1L);
        request.setVariId(2L);
        request.setProdTypeValue("Updated Value");

        ProductTypeVariation existingVariation = new ProductTypeVariation();
        ProductType productType = new ProductType();
        Variation variation = new Variation();

        when(repository.findById(id)).thenReturn(Optional.of(existingVariation));
        when(productTypeService.findProductTypeById(1L)).thenReturn(productType);
        when(variationService.getById(2L)).thenReturn(variation);
        when(repository.save(any(ProductTypeVariation.class))).thenReturn(existingVariation);

        ProductTypeVariation result = productTypeVariationService.update(id, request);

        assertNotNull(result);
        assertEquals(productType, result.getProdTypeId());
        assertEquals(variation, result.getVariId());
        assertEquals("Updated Value", result.getProdTypeValue());
        verify(repository).findById(id);
        verify(productTypeService).findProductTypeById(1L);
        verify(variationService).getById(2L);
        verify(repository).save(any(ProductTypeVariation.class));
    }

    @Test
    void update_NotFound_ReturnsNull() {
        Long id = 1L;
        ProductTypeVariationRequest request = new ProductTypeVariationRequest();

        when(repository.findById(id)).thenReturn(Optional.empty());

        ProductTypeVariation result = productTypeVariationService.update(id, request);

        assertNull(result);
        verify(repository).findById(id);
        verify(productTypeService, never()).findProductTypeById(anyLong());
        verify(variationService, never()).getById(anyLong());
    }

    @Test
    void update_VariationNotFound_ReturnsNull() {
        Long id = 1L;
        ProductTypeVariationRequest request = new ProductTypeVariationRequest();
        request.setProdTypeId(1L);
        request.setVariId(2L);

        ProductTypeVariation existingVariation = new ProductTypeVariation();
        ProductType productType = new ProductType();

        when(repository.findById(id)).thenReturn(Optional.of(existingVariation));
        when(productTypeService.findProductTypeById(1L)).thenReturn(productType);
        when(variationService.getById(2L)).thenReturn(null);

        ProductTypeVariation result = productTypeVariationService.update(id, request);

        assertNull(result);
        verify(repository).findById(id);
        verify(productTypeService).findProductTypeById(1L);
        verify(variationService).getById(2L);
        verify(repository, never()).save(any(ProductTypeVariation.class));
    }

    // Test for delete
    @Test
    void delete_Success() {
        Long id = 1L;

        doNothing().when(repository).deleteById(id);

        boolean result = productTypeVariationService.delete(id);

        assertTrue(result);
        verify(repository).deleteById(id);
    }
}