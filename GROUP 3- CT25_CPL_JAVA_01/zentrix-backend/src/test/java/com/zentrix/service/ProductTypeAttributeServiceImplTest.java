package com.zentrix.service;

import com.zentrix.model.entity.Attribute;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.ProductTypeAttribute;
import com.zentrix.model.request.ProductTypeAttributeRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.ProductTypeAttributeRepository;
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
class ProductTypeAttributeServiceImplTest {

    @Mock
    private ProductTypeAttributeRepository repository;

    @Mock
    private ProductTypeService productTypeService;

    @Mock
    private AttributeService attributeService;

    @InjectMocks
    private ProductTypeAttributeServiceImpl productTypeAttributeService;

    @BeforeEach
    void setUp() {
        reset(repository, productTypeService, attributeService);
    }

    // Test for isAttributeUsed
    @Test
    void isAttributeUsed_AttributeExists_ReturnsTrue() {
        Long atbId = 1L;
        Attribute attribute = new Attribute();
        attribute.setAtbId(atbId);

        when(repository.existsByAtbId(attribute)).thenReturn(true);

        boolean result = productTypeAttributeService.isAttributeUsed(atbId);

        assertTrue(result);
        verify(repository).existsByAtbId(attribute);
    }

    @Test
    void isAttributeUsed_AttributeNotExists_ReturnsFalse() {
        Long atbId = 1L;
        Attribute attribute = new Attribute();
        attribute.setAtbId(atbId);

        when(repository.existsByAtbId(attribute)).thenReturn(false);

        boolean result = productTypeAttributeService.isAttributeUsed(atbId);

        assertFalse(result);
        verify(repository).existsByAtbId(attribute);
    }

    // Test for getAll
    @Test
    void getAll_Success() {
        int page = 0;
        int size = 10;
        List<ProductTypeAttribute> attributes = Arrays.asList(new ProductTypeAttribute(), new ProductTypeAttribute());
        Page<ProductTypeAttribute> attributePage = new PageImpl<>(attributes, PageRequest.of(page, size),
                attributes.size());

        when(repository.findAll(any(Pageable.class))).thenReturn(attributePage);

        PaginationWrapper<List<ProductTypeAttribute>> result = productTypeAttributeService.getAll(page, size);

        assertNotNull(result);
        assertEquals(attributes, result.getData());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(attributes.size(), result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(repository).findAll(PageRequest.of(page, size));
    }

    // Test for getById
    @Test
    void getById_Success() {
        Long id = 1L;
        ProductTypeAttribute productTypeAttribute = new ProductTypeAttribute();

        when(repository.findById(id)).thenReturn(Optional.of(productTypeAttribute));

        ProductTypeAttribute result = productTypeAttributeService.getById(id);

        assertNotNull(result);
        assertEquals(productTypeAttribute, result);
        verify(repository).findById(id);
    }

    @Test
    void getById_NotFound_ReturnsNull() {
        Long id = 1L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        ProductTypeAttribute result = productTypeAttributeService.getById(id);

        assertNull(result);
        verify(repository).findById(id);
    }

    // Test for create
    @Test
    void create_Success() {
        ProductTypeAttributeRequest request = new ProductTypeAttributeRequest();
        request.setProdTypeId(1L);
        request.setAtbId(2L);
        request.setProdAtbValue("Value");
        request.setAtbDescription("Description");

        ProductType productType = new ProductType();
        Attribute attribute = new Attribute();
        ProductTypeAttribute productTypeAttribute = new ProductTypeAttribute();
        productTypeAttribute.setProdTypeId(productType);
        productTypeAttribute.setAtbId(attribute);
        productTypeAttribute.setProdAtbValue("Value");
        productTypeAttribute.setAtbDescription("Description");

        when(productTypeService.findProductTypeById(1L)).thenReturn(productType);
        when(attributeService.getById(2L)).thenReturn(attribute);
        when(repository.save(any(ProductTypeAttribute.class))).thenReturn(productTypeAttribute);

        ProductTypeAttribute result = productTypeAttributeService.create(request);

        assertNotNull(result);
        assertEquals(productType, result.getProdTypeId());
        assertEquals(attribute, result.getAtbId());
        assertEquals("Value", result.getProdAtbValue());
        assertEquals("Description", result.getAtbDescription());
        verify(productTypeService).findProductTypeById(1L);
        verify(attributeService).getById(2L);
        verify(repository).save(any(ProductTypeAttribute.class));
    }

    @Test
    void create_ProductTypeNotFound_ReturnsNull() {
        ProductTypeAttributeRequest request = new ProductTypeAttributeRequest();
        request.setProdTypeId(1L);
        request.setAtbId(2L);

        when(productTypeService.findProductTypeById(1L)).thenReturn(null);

        ProductTypeAttribute result = productTypeAttributeService.create(request);

        assertNull(result);
        verify(productTypeService).findProductTypeById(1L);
        verify(attributeService, never()).getById(anyLong());
        verify(repository, never()).save(any(ProductTypeAttribute.class));
    }

    @Test
    void create_AttributeNotFound_ReturnsNull() {
        ProductTypeAttributeRequest request = new ProductTypeAttributeRequest();
        request.setProdTypeId(1L);
        request.setAtbId(2L);

        ProductType productType = new ProductType();

        when(productTypeService.findProductTypeById(1L)).thenReturn(productType);
        when(attributeService.getById(2L)).thenReturn(null);

        ProductTypeAttribute result = productTypeAttributeService.create(request);

        assertNull(result);
        verify(productTypeService).findProductTypeById(1L);
        verify(attributeService).getById(2L);
        verify(repository, never()).save(any(ProductTypeAttribute.class));
    }

    // Test for update
    @Test
    void update_Success() {
        Long id = 1L;
        ProductTypeAttributeRequest request = new ProductTypeAttributeRequest();
        request.setProdAtbValue("Updated Value");
        request.setAtbDescription("Updated Description");

        ProductTypeAttribute existingAttribute = new ProductTypeAttribute();
        ProductTypeAttribute updatedAttribute = new ProductTypeAttribute();
        updatedAttribute.setProdAtbValue("Updated Value");
        updatedAttribute.setAtbDescription("Updated Description");

        when(repository.findById(id)).thenReturn(Optional.of(existingAttribute));
        when(repository.save(any(ProductTypeAttribute.class))).thenReturn(updatedAttribute);

        ProductTypeAttribute result = productTypeAttributeService.update(id, request);

        assertNotNull(result);
        assertEquals("Updated Value", result.getProdAtbValue());
        assertEquals("Updated Description", result.getAtbDescription());
        verify(repository).findById(id);
        verify(repository).save(any(ProductTypeAttribute.class));
    }

    @Test
    void update_NotFound_ReturnsNull() {
        Long id = 1L;
        ProductTypeAttributeRequest request = new ProductTypeAttributeRequest();

        when(repository.findById(id)).thenReturn(Optional.empty());

        ProductTypeAttribute result = productTypeAttributeService.update(id, request);

        assertNull(result);
        verify(repository).findById(id);
        verify(repository, never()).save(any(ProductTypeAttribute.class));
    }

    // Test for delete
    @Test
    void delete_Success() {
        Long id = 1L;

        when(repository.existsById(id)).thenReturn(true);
        doNothing().when(repository).deleteById(id);

        boolean result = productTypeAttributeService.delete(id);

        assertTrue(result);
        verify(repository).existsById(id);
        verify(repository).deleteById(id);
    }

    @Test
    void delete_NotFound_ReturnsFalse() {
        Long id = 1L;

        when(repository.existsById(id)).thenReturn(false);

        boolean result = productTypeAttributeService.delete(id);

        assertFalse(result);
        verify(repository).existsById(id);
        verify(repository, never()).deleteById(id);
    }
}