package com.zentrix.controller;

import com.zentrix.model.entity.Supplier;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/*
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date April 01, 2025
 */

public class SupplierControllerTest {

    @InjectMocks
    private SupplierController supplierController;

    @Mock
    private SupplierService supplierService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllSuppliers_emptyPage_returnsNoSuppliersFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> emptyPage = new PageImpl<>(Collections.emptyList());
        when(supplierService.getAllSuppliers(pageable)).thenReturn(emptyPage);

        // Act
        ResponseEntity<ResponseObject<Page<Supplier>>> response = supplierController.getAllSuppliers(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("No suppliers found", response.getBody().getMessage());
        assertEquals(emptyPage, response.getBody().getContent());
        verify(supplierService, times(1)).getAllSuppliers(pageable);
    }

    @Test
    void getAllSuppliers_withData_returnsSuccess() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Supplier> suppliers = List.of(new Supplier());
        Page<Supplier> supplierPage = new PageImpl<>(suppliers);
        when(supplierService.getAllSuppliers(pageable)).thenReturn(supplierPage);

        // Act
        ResponseEntity<ResponseObject<Page<Supplier>>> response = supplierController.getAllSuppliers(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(supplierPage, response.getBody().getContent());
        verify(supplierService, times(1)).getAllSuppliers(pageable);
    }

    @Test
    void getSupplierById_notFound_returnsNotFound() {
        // Arrange
        int id = 1;
        when(supplierService.getSupplierById(id)).thenReturn(null);

        // Act
        ResponseEntity<ResponseObject<Supplier>> response = supplierController.getSupplierById(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Supplier Exception: Supplier not found", response.getBody().getMessage());
        assertEquals(null, response.getBody().getContent());
        verify(supplierService, times(1)).getSupplierById(id);
    }

    @Test
    void getSupplierById_found_returnsSuccess() {
        // Arrange
        int id = 1;
        Supplier supplier = new Supplier();
        when(supplierService.getSupplierById(id)).thenReturn(supplier);

        // Act
        ResponseEntity<ResponseObject<Supplier>> response = supplierController.getSupplierById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(supplier, response.getBody().getContent());
        verify(supplierService, times(1)).getSupplierById(id);
    }

    @Test
    void searchSuppliersByName_noResults_returnsNotFound() {
        // Arrange
        String name = "test";
        when(supplierService.findSuppliersByName(name)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ResponseObject<List<Supplier>>> response = supplierController.searchSuppliersByName(name);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Supplier Exception: No suppliers found with the given name", response.getBody().getMessage());
        assertEquals(null, response.getBody().getContent());
        verify(supplierService, times(1)).findSuppliersByName(name);
    }

    @Test
    void searchSuppliersByName_withResults_returnsSuccess() {
        // Arrange
        String name = "test";
        List<Supplier> suppliers = List.of(new Supplier());
        when(supplierService.findSuppliersByName(name)).thenReturn(suppliers);

        // Act
        ResponseEntity<ResponseObject<List<Supplier>>> response = supplierController.searchSuppliersByName(name);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(suppliers, response.getBody().getContent());
        verify(supplierService, times(1)).findSuppliersByName(name);
    }
}