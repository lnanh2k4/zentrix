package com.zentrix.service;

import com.zentrix.model.entity.Supplier;
import com.zentrix.model.request.SupplierRequest;
import com.zentrix.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/*
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date April 01, 2025
 */

public class SupplierServiceImplTest {

    @InjectMocks
    private SupplierServiceImpl supplierService;

    @Mock
    private SupplierRepository supplierRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllSuppliers_returnsPaginatedSuppliers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Supplier> suppliers = List.of(new Supplier());
        Page<Supplier> supplierPage = new PageImpl<>(suppliers);
        when(supplierRepository.findAll(pageable)).thenReturn(supplierPage);

        // Act
        Page<Supplier> result = supplierService.getAllSuppliers(pageable);

        // Assert
        assertEquals(supplierPage, result);
        verify(supplierRepository, times(1)).findAll(pageable);
    }

    @Test
    void getSupplierById_found_returnsSupplier() {
        // Arrange
        int id = 1;
        Supplier supplier = new Supplier();
        when(supplierRepository.findById(id)).thenReturn(Optional.of(supplier));

        // Act
        Supplier result = supplierService.getSupplierById(id);

        // Assert
        assertEquals(supplier, result);
        verify(supplierRepository, times(1)).findById(id);
    }

    @Test
    void getSupplierById_notFound_returnsNull() {
        // Arrange
        int id = 1;
        when(supplierRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Supplier result = supplierService.getSupplierById(id);

        // Assert
        assertNull(result);
        verify(supplierRepository, times(1)).findById(id);
    }

    @Test
    void addSupplier_emailExists_returnsNull() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setEmail("test@example.com");
        when(supplierRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        Supplier result = supplierService.addSupplier(supplier);

        // Assert
        assertNull(result);
        verify(supplierRepository, times(1)).existsByEmail("test@example.com");
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void addSupplier_success_returnsSavedSupplier() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setEmail("test@example.com");
        when(supplierRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(supplierRepository.save(supplier)).thenReturn(supplier);

        // Act
        Supplier result = supplierService.addSupplier(supplier);

        // Assert
        assertEquals(supplier, result);
        verify(supplierRepository, times(1)).existsByEmail("test@example.com");
        verify(supplierRepository, times(1)).save(supplier);
    }

    @Test
    void findSuppliersByName_returnsMatchingSuppliers() {
        // Arrange
        String name = "test";
        List<Supplier> suppliers = List.of(new Supplier());
        when(supplierRepository.findBySuppNameContainingIgnoreCase(name)).thenReturn(suppliers);

        // Act
        List<Supplier> result = supplierService.findSuppliersByName(name);

        // Assert
        assertEquals(suppliers, result);
        verify(supplierRepository, times(1)).findBySuppNameContainingIgnoreCase(name);
    }

    @Test
    void updateSupplier_notFound_returnsNull() {
        // Arrange
        int id = 1;
        SupplierRequest request = new SupplierRequest("Updated", "updated@example.com", "123", "Address");
        when(supplierRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Supplier result = supplierService.updateSupplier(id, request);

        // Assert
        assertNull(result);
        verify(supplierRepository, times(1)).findById(id);
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void updateSupplier_success_returnsUpdatedSupplier() {
        // Arrange
        int id = 1;
        Supplier existingSupplier = new Supplier();
        SupplierRequest request = new SupplierRequest("Updated", "updated@example.com", "123", "Address");
        when(supplierRepository.findById(id)).thenReturn(Optional.of(existingSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Supplier result = supplierService.updateSupplier(id, request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated", result.getSuppName());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("123", result.getPhone());
        assertEquals("Address", result.getAddress());
        verify(supplierRepository, times(1)).findById(id);
        verify(supplierRepository, times(1)).save(existingSupplier);
    }

    @Test
    void deleteSupplier_notFound_returnsFalse() {
        // Arrange
        int id = 1;
        when(supplierRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        boolean result = supplierService.deleteSupplier(id);

        // Assert
        assertFalse(result);
        verify(supplierRepository, times(1)).findById(id);
        verify(supplierRepository, never()).delete(any(Supplier.class));
    }

    @Test
    void deleteSupplier_success_returnsTrue() {
        // Arrange
        int id = 1;
        Supplier supplier = new Supplier();
        when(supplierRepository.findById(id)).thenReturn(Optional.of(supplier));
        when(supplierRepository.existsById(id)).thenReturn(false); // After deletion

        

    }
}