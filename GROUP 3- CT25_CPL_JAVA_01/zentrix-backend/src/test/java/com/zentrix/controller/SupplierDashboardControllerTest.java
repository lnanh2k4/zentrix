package com.zentrix.controller;

import com.zentrix.model.entity.Supplier;
import com.zentrix.model.request.SupplierRequest;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/*
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date April 01, 2025
 */

public class SupplierDashboardControllerTest {

    @InjectMocks
    private SupplierDashboardController supplierDashboardController;

    @Mock
    private SupplierService supplierService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createSupplier_success_returnsCreated() {
        // Arrange
        SupplierRequest request = new SupplierRequest("Test Supplier", "test@example.com", "123456789", "Address");
        Supplier supplier = new Supplier();
        supplier.setSuppName(request.getSuppName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        when(supplierService.addSupplier(any(Supplier.class))).thenReturn(supplier);

        // Act
        ResponseEntity<ResponseObject<Supplier>> response = supplierDashboardController.createSupplier(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Supplier Exception: Supplier created successfully", response.getBody().getMessage());
        assertEquals(supplier, response.getBody().getContent());
        verify(supplierService, times(1)).addSupplier(any(Supplier.class));
    }

    @Test
    void createSupplier_failure_returnsBadRequest() {
        // Arrange
        SupplierRequest request = new SupplierRequest("Test Supplier", "test@example.com", "123456789", "Address");
        when(supplierService.addSupplier(any(Supplier.class))).thenThrow(new RuntimeException());

        // Act
        ResponseEntity<ResponseObject<Supplier>> response = supplierDashboardController.createSupplier(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Supplier Exception: Failed to create new supplier", response.getBody().getMessage());
        assertEquals(null, response.getBody().getContent());
        verify(supplierService, times(1)).addSupplier(any(Supplier.class));
    }

    @Test
    void updateSupplier_notFound_returnsNotFound() {
        // Arrange
        int id = 1;
        SupplierRequest request = new SupplierRequest("Updated Supplier", "updated@example.com", "987654321", "New Address");
        when(supplierService.updateSupplier(id, request)).thenReturn(null);

        // Act
        ResponseEntity<ResponseObject<Supplier>> response = supplierDashboardController.updateSupplier(id, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Supplier Exception: Failed to update supplier", response.getBody().getMessage());
        assertEquals(null, response.getBody().getContent());
        verify(supplierService, times(1)).updateSupplier(id, request);
    }

    @Test
    void updateSupplier_success_returnsOk() {
        // Arrange
        int id = 1;
        SupplierRequest request = new SupplierRequest("Updated Supplier", "updated@example.com", "987654321", "New Address");
        Supplier updatedSupplier = new Supplier();
        when(supplierService.updateSupplier(id, request)).thenReturn(updatedSupplier);

        // Act
        ResponseEntity<ResponseObject<Supplier>> response = supplierDashboardController.updateSupplier(id, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Supplier updated successfully", response.getBody().getMessage());
        assertEquals(updatedSupplier, response.getBody().getContent());
        verify(supplierService, times(1)).updateSupplier(id, request);
    }

    @Test
    void deleteSupplier_notFound_returnsNotFound() {
        // Arrange
        int id = 1;
        when(supplierService.deleteSupplier(id)).thenReturn(false);

        // Act
        ResponseEntity<ResponseObject<Void>> response = supplierDashboardController.deleteSupplier(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Supplier Exception: Supplier not found", response.getBody().getMessage());
        verify(supplierService, times(1)).deleteSupplier(id);
    }

    @Test
    void deleteSupplier_success_returnsOk() {
        // Arrange
        int id = 1;
        when(supplierService.deleteSupplier(id)).thenReturn(true);

        // Act
        ResponseEntity<ResponseObject<Void>> response = supplierDashboardController.deleteSupplier(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Supplier deleted successfully", response.getBody().getMessage());
        verify(supplierService, times(1)).deleteSupplier(id);
    }
}