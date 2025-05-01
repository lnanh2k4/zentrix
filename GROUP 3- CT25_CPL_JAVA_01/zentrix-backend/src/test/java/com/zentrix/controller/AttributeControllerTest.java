package com.zentrix.controller;

import com.zentrix.model.entity.Attribute;
import com.zentrix.model.request.AttributeRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.AttributeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttributeControllerTest {

    @Mock
    private AttributeService attributeService;

    @InjectMocks
    private AttributeController attributeController;

    @BeforeEach
    void setUp() {
        reset(attributeService);
    }

    // Test for getAttributeById
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getAttributeById_Success() {
        Long id = 1L;
        Attribute attribute = new Attribute();

        when(attributeService.getById(id)).thenReturn(attribute);

        ResponseEntity<Attribute> response = attributeController.getAttributeById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(attribute, response.getBody());
        verify(attributeService).getById(id);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getAttributeById_NotFound() {
        Long id = 1L;

        when(attributeService.getById(id)).thenReturn(null);

        ResponseEntity<Attribute> response = attributeController.getAttributeById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(attributeService).getById(id);
    }

    // Test for getAllAttributes
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getAllAttributes_Success() {
        List<Attribute> attributes = Arrays.asList(new Attribute());
        PaginationWrapper<List<Attribute>> wrapper = new PaginationWrapper.Builder<List<Attribute>>()
                .setData(attributes)
                .setPage(0)
                .setSize(1000)
                .setTotalPages(1)
                .setTotalElements(attributes.size())
                .build();

        when(attributeService.getAll(0, 1000)).thenReturn(wrapper);

        ResponseEntity<ResponseObject<List<Attribute>>> response = attributeController.getAllAttributes(0, 1000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(attributes, response.getBody().getContent());
        assertEquals("Get all attributes successfully!", response.getBody().getMessage());
        assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
        verify(attributeService).getAll(0, 1000);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getAllAttributes_InvalidPaginationParameters_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            attributeController.getAllAttributes(-1, 1000);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            attributeController.getAllAttributes(0, 0);
        });

        verify(attributeService, never()).getAll(anyInt(), anyInt());
    }

    // Test for createAttribute
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createAttribute_Success() {
        AttributeRequest request = new AttributeRequest();
        Attribute attribute = new Attribute();

        when(attributeService.create(request)).thenReturn(attribute);

        ResponseEntity<Attribute> response = attributeController.createAttribute(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(attribute, response.getBody());
        verify(attributeService).create(request);
    }

    // Test for deleteAttribute
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteAttribute_Success() {
        Long id = 1L;

        when(attributeService.delete(id)).thenReturn(true);

        ResponseEntity<Void> response = attributeController.deleteAttribute(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(attributeService).delete(id);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteAttribute_NotFound() {
        Long id = 1L;

        when(attributeService.delete(id)).thenReturn(false);

        ResponseEntity<Void> response = attributeController.deleteAttribute(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(attributeService).delete(id);
    }

    // Test for updateAttribute
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateAttribute_Success() {
        Long id = 1L;
        Attribute attribute = new Attribute();
        Attribute updatedAttribute = new Attribute();

        when(attributeService.update(id, attribute)).thenReturn(updatedAttribute);

        ResponseEntity<Attribute> response = attributeController.updateAttribute(id, attribute);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedAttribute, response.getBody());
        verify(attributeService).update(id, attribute);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateAttribute_NotFound() {
        Long id = 1L;
        Attribute attribute = new Attribute();

        when(attributeService.update(id, attribute)).thenReturn(null);

        ResponseEntity<Attribute> response = attributeController.updateAttribute(id, attribute);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(attributeService).update(id, attribute);
    }
}