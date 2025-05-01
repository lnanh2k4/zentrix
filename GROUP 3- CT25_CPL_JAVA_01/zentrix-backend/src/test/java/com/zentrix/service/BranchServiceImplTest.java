package com.zentrix.service;

import com.zentrix.model.entity.Branch;
import com.zentrix.model.entity.ProductTypeBranch;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.BranchRepository;
import com.zentrix.repository.ProductTypeBranchRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test class for BranchServiceImpl.
 * Tests the business logic using Mockito to mock repositories.
 */
class BranchServiceImplTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductTypeBranchRepository productTypeBranchRepository;

    @InjectMocks
    private BranchServiceImpl branchService;

    @BeforeEach
    void setUp() {
        // Initialize Mockito
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllBranchesNonPaged_Success() {
        // Mock data
        List<Branch> branches = Arrays.asList(
                new Branch(1L, "Branch 1", "123 Street", "555-1234", 1, null, null, null),
                new Branch(2L, "Branch 2", "456 Street", "555-5678", 1, null, null, null)
        );

        // Mock repository behavior
        when(branchRepository.findAll()).thenReturn(branches);

        // Call service method
        List<Branch> result = branchService.getAllBranchesNonPaged();

        // Verify results
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Branch 1", result.get(0).getBrchName());
        assertEquals("Branch 2", result.get(1).getBrchName());

        // Verify repository was called
        verify(branchRepository, times(1)).findAll();
    }

    @Test
    void testGetAllBranches_Success() {
        // Mock data
        List<Branch> branches = Arrays.asList(
                new Branch(1L, "Branch 1", "123 Street", "555-1234", 1, null, null, null),
                new Branch(2L, "Branch 2", "456 Street", "555-5678", 1, null, null, null)
        );
        Page<Branch> branchPage = new PageImpl<>(branches, PageRequest.of(0, 10), 2);

        // Mock repository behavior
        when(branchRepository.findAll(any(Pageable.class))).thenReturn(branchPage);

        // Call service method
        PaginationWrapper<List<Branch>> result = branchService.getAllBranches(0, 10);

        // Verify results
        assertNotNull(result);
        
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getTotalElements());

        // Verify repository was called
        verify(branchRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testGetBranchById_Success() {
        // Mock data
        Branch branch = new Branch(1L, "Branch 1", "123 Street", "555-1234", 1, null, null, null);

        // Mock repository behavior
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        // Call service method
        Branch result = branchService.getBranchById(1L);

        // Verify results
        assertNotNull(result);
        assertEquals(1L, result.getBrchId());
        assertEquals("Branch 1", result.getBrchName());

        // Verify repository was called
        verify(branchRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBranchById_NotFound() {
        // Mock repository behavior
        when(branchRepository.findById(1L)).thenReturn(Optional.empty());

        // Call service method
        Branch result = branchService.getBranchById(1L);

        // Verify results
        assertNull(result);

        // Verify repository was called
        verify(branchRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateBranch_Success() {
        // Mock data
        Branch branch = new Branch(null, "New Branch", "789 Street", "555-9012", 1, null, null, null);
        Branch savedBranch = new Branch(1L, "New Branch", "789 Street", "555-9012", 1, null, null, null);

        // Mock repository behavior
        when(branchRepository.save(any(Branch.class))).thenReturn(savedBranch);

        // Call service method
        Branch result = branchService.createBranch(branch);

        // Verify results
        assertNotNull(result);
        assertEquals(1L, result.getBrchId());
        assertEquals("New Branch", result.getBrchName());

        // Verify repository was called
        verify(branchRepository, times(1)).save(any(Branch.class));
    }

    @Test
    void testUpdateBranch_Success() {
        // Mock data
        Branch existingBranch = new Branch(1L, "Branch 1", "123 Street", "555-1234", 1, null, null, null);
        Branch updatedBranch = new Branch(1L, "Updated Branch", "789 Street", "555-9012", 0, null, null, null);

        // Mock repository behavior
        when(branchRepository.findById(1L)).thenReturn(Optional.of(existingBranch));
        when(branchRepository.save(any(Branch.class))).thenReturn(updatedBranch);

        // Call service method
        Branch result = branchService.updateBranch(1L, updatedBranch);

        // Verify results
        assertNotNull(result);
        assertEquals(1L, result.getBrchId());
        assertEquals("Updated Branch", result.getBrchName());
        assertEquals("789 Street", result.getAddress());
        assertEquals(0, result.getStatus());

        // Verify repository was called
        verify(branchRepository, times(1)).findById(1L);
        verify(branchRepository, times(1)).save(any(Branch.class));
    }

    @Test
    void testUpdateBranch_NotFound() {
        // Mock data
        Branch updatedBranch = new Branch(1L, "Updated Branch", "789 Street", "555-9012", 0, null, null, null);

        // Mock repository behavior
        when(branchRepository.findById(1L)).thenReturn(Optional.empty());

        // Call service method and expect exception
        assertThrows(Exception.class, () -> branchService.updateBranch(1L, updatedBranch));

        // Verify repository was called
        verify(branchRepository, times(1)).findById(1L);
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    void testDeleteBranch_Success() {
        // Mock data
        Branch branch = new Branch(1L, "Branch 1", "123 Street", "555-1234", 1, null, null, null);

        // Mock repository behavior
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(productTypeBranchRepository.existsByBrchId_BrchId(1L)).thenReturn(false);
        doNothing().when(branchRepository).deleteById(1L);

        // Call service method
        boolean result = branchService.deleteBranch(1L);

        // Verify results
        assertTrue(result);

        // Verify repository was called
        verify(branchRepository, times(1)).findById(1L);
        verify(productTypeBranchRepository, times(1)).existsByBrchId_BrchId(1L);
        verify(branchRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteBranch_NotFound() {
        // Mock repository behavior
        when(branchRepository.findById(1L)).thenReturn(Optional.empty());

        // Call service method and expect exception
        assertThrows(ResponseStatusException.class, () -> branchService.deleteBranch(1L));

        // Verify repository was called
        verify(branchRepository, times(1)).findById(1L);
        verify(productTypeBranchRepository, never()).existsByBrchId_BrchId(1L);
        verify(branchRepository, never()).deleteById(1L);
    }

    @Test
    void testDeleteBranch_HasProductTypes() {
        // Mock data
        Branch branch = new Branch(1L, "Branch 1", "123 Street", "555-1234", 1, null, null, null);

        // Mock repository behavior
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(productTypeBranchRepository.existsByBrchId_BrchId(1L)).thenReturn(true);

        // Call service method and expect exception
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> branchService.deleteBranch(1L));
        assertEquals("Cannot delete branch because it contains product types", exception.getMessage());

        // Verify repository was called
        verify(branchRepository, times(1)).findById(1L);
        verify(productTypeBranchRepository, times(1)).existsByBrchId_BrchId(1L);
        verify(branchRepository, never()).deleteById(1L);
    }

    @Test
    void testFindBranchesByName_Success() {
        // Mock data
        List<Branch> branches = Arrays.asList(
                new Branch(1L, "Branch 1", "123 Street", "555-1234", 1, null, null, null),
                new Branch(2L, "Branch 2", "456 Street", "555-5678", 1, null, null, null)
        );

        // Mock repository behavior
        when(branchRepository.findByBrchNameContainingIgnoreCase("Branch")).thenReturn(branches);

        // Call service method
        List<Branch> result = branchService.findBranchesByName("Branch");

        // Verify results
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Branch 1", result.get(0).getBrchName());
        assertEquals("Branch 2", result.get(1).getBrchName());

        // Verify repository was called
        verify(branchRepository, times(1)).findByBrchNameContainingIgnoreCase("Branch");
    }

    @Test
    void testFindBranchesByName_EmptyName() {
        // Call service method and expect exception
        Exception exception = assertThrows(Exception.class, () -> branchService.findBranchesByName(""));
        assertNotNull(exception);

        // Verify repository was not called
        verify(branchRepository, never()).findByBrchNameContainingIgnoreCase(anyString());
    }

    @Test
    void testFindByBrchName_Success() {
        // Mock data
        Branch branch = new Branch(1L, "Branch 1", "123 Street", "555-1234", 1, null, null, null);

        // Mock repository behavior
        when(branchRepository.findByBrchName("Branch 1")).thenReturn(Optional.of(branch));

        // Call service method
        Branch result = branchService.findByBrchName("Branch 1");

        // Verify results
        assertNotNull(result);
        assertEquals("Branch 1", result.getBrchName());

        // Verify repository was called
        verify(branchRepository, times(1)).findByBrchName("Branch 1");
    }

    @Test
    void testFindByBrchName_NotFound() {
        // Mock repository behavior
        when(branchRepository.findByBrchName("Branch 1")).thenReturn(Optional.empty());

        // Call service method
        Branch result = branchService.findByBrchName("Branch 1");

        // Verify results
        assertNull(result);

        // Verify repository was called
        verify(branchRepository, times(1)).findByBrchName("Branch 1");
    }

    @Test
    void testGetProductTypesByBranchId_Success() {
        // Mock data
        List<ProductTypeBranch> productTypes = Arrays.asList(
                new ProductTypeBranch(),
                new ProductTypeBranch()
        );

        // Mock repository behavior
        when(productTypeBranchRepository.findByBrchId_BrchId(1L)).thenReturn(productTypes);

        // Call service method
        List<ProductTypeBranch> result = branchService.getProductTypesByBranchId(1L);

        // Verify results
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify repository was called
        verify(productTypeBranchRepository, times(1)).findByBrchId_BrchId(1L);
    }
}