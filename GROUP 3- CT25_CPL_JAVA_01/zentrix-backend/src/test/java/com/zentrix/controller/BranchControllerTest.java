package com.zentrix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zentrix.model.entity.Branch;
import com.zentrix.model.request.BranchRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.BranchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test class for BranchController.
 * Tests the REST endpoints using MockMvc and Mockito.
 */
public class BranchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BranchService branchService;

    @InjectMocks
    private BranchController branchController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Initialize Mockito
        MockitoAnnotations.openMocks(this);
        // Initialize MockMvc to simulate HTTP requests
        mockMvc = MockMvcBuilders.standaloneSetup(branchController).build();
        // Initialize ObjectMapper for JSON conversion
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetAllBranches_Success() throws Exception {
        // Mock data
        List<Branch> branches = Arrays.asList(
                new Branch(1L, "Branch 1", "123 Street", "555-1234", 1, null, null, null),
                new Branch(2L, "Branch 2", "456 Street", "555-5678", 1, null, null, null)
        );
        PaginationWrapper<List<Branch>> paginationWrapper = new PaginationWrapper<>(branches, 0, 10, 1, 2);

        // Mock branchService behavior
        when(branchService.getAllBranches(0, 10)).thenReturn(paginationWrapper);

        // Perform GET request and verify results
        mockMvc.perform(get("/api/v1/branches")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("List of all branches retrieved successfully"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.content[0].brchId").value(1))
                .andExpect(jsonPath("$.content[1].brchId").value(2))
                .andExpect(jsonPath("$.content.length()").value(2));

        // Verify branchService was called
        verify(branchService, times(1)).getAllBranches(0, 10);
    }

    @Test
    void testGetBranchById_Success() throws Exception {
        // Mock data
        Branch branch = new Branch(1L, "Branch 1", "123 Street", "555-1234", 1, null, null, null);

        // Mock branchService behavior
        when(branchService.getBranchById(1L)).thenReturn(branch);

        // Perform GET request and verify results
        mockMvc.perform(get("/api/v1/branches/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.content.brchId").value(1))
                .andExpect(jsonPath("$.content.brchName").value("Branch 1"));

        // Verify branchService was called
        verify(branchService, times(1)).getBranchById(1L);
    }

    @Test
    void testGetBranchById_NotFound() throws Exception {
        // Mock branchService returning null
        when(branchService.getBranchById(1L)).thenReturn(null);

        // Perform GET request and verify results
        mockMvc.perform(get("/api/v1/branches/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Branch Exception: Branch not found"))
                .andExpect(jsonPath("$.code").value(1100))
                .andExpect(jsonPath("$.content").isEmpty());

        // Verify branchService was called
        verify(branchService, times(1)).getBranchById(1L);
    }

    @Test
    void testCreateBranch_Success() throws Exception {
        // Mock data
        BranchRequest branchRequest = new BranchRequest("New Branch", "789 Street", "555-9012", 1);
        Branch createdBranch = new Branch(1L, "New Branch", "789 Street", "555-9012", 1, null, null, null);

        // Mock branchService behavior
        when(branchService.findBranchesByName("New Branch")).thenReturn(Arrays.asList());
        when(branchService.createBranch(any(Branch.class))).thenReturn(createdBranch);

        // Perform POST request and verify results
        mockMvc.perform(post("/api/v1/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(branchRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Branch created successfully"))
                .andExpect(jsonPath("$.code").value(1109))
                .andExpect(jsonPath("$.content.brchId").value(1))
                .andExpect(jsonPath("$.content.brchName").value("New Branch"));

        // Verify branchService was called
        verify(branchService, times(1)).findBranchesByName("New Branch");
        verify(branchService, times(1)).createBranch(any(Branch.class));
    }

    @Test
    void testCreateBranch_DuplicateName() throws Exception {
        // Mock data
        BranchRequest branchRequest = new BranchRequest("Existing Branch", "789 Street", "555-9012", 1);
        Branch existingBranch = new Branch(1L, "Existing Branch", "123 Street", "555-1234", 1, null, null, null);

        // Mock branchService behavior
        when(branchService.findBranchesByName("Existing Branch")).thenReturn(Arrays.asList(existingBranch));

        // Perform POST request and verify results
        mockMvc.perform(post("/api/v1/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(branchRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Branch Exception: Branch name 'Existing Branch' is already taken"))
                .andExpect(jsonPath("$.code").value(1106))
                .andExpect(jsonPath("$.content").isEmpty());

        // Verify branchService was called
        verify(branchService, times(1)).findBranchesByName("Existing Branch");
        // Verify createBranch was not called
        verify(branchService, never()).createBranch(any(Branch.class));
    }

    @Test
    void testUpdateBranch_Success() throws Exception {
        // Mock data
        BranchRequest branchRequest = new BranchRequest("Updated Branch", "789 Street", "555-9012", 1);
        Branch existingBranch = new Branch(1L, "Branch 1", "123 Street", "555-1234", 1, null, null, null);
        Branch updatedBranch = new Branch(1L, "Updated Branch", "789 Street", "555-9012", 1, null, null, null);

        // Mock branchService behavior
        when(branchService.getBranchById(1L)).thenReturn(existingBranch);
        when(branchService.findByBrchName("Updated Branch")).thenReturn(null);
        when(branchService.updateBranch(eq(1L), any(Branch.class))).thenReturn(updatedBranch);

        // Perform PUT request and verify results
        mockMvc.perform(put("/api/v1/branches/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(branchRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Branch updated successfully"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.content.brchId").value(1))
                .andExpect(jsonPath("$.content.brchName").value("Updated Branch"));

        // Verify branchService was called
        verify(branchService, times(1)).getBranchById(1L);
        verify(branchService, times(1)).findByBrchName("Updated Branch");
        verify(branchService, times(1)).updateBranch(eq(1L), any(Branch.class));
    }

    @Test
    void testDeleteBranch_Success() throws Exception {
        // Mock branchService behavior
        when(branchService.deleteBranch(1L)).thenReturn(true);

        // Perform DELETE request and verify results
        mockMvc.perform(delete("/api/v1/branches/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Branch deleted successfully"))
                .andExpect(jsonPath("$.code").value(1109))
                .andExpect(jsonPath("$.content").isEmpty());

        // Verify branchService was called
        verify(branchService, times(1)).deleteBranch(1L);
    }

    @Test
    void testDeleteBranch_NotFound() throws Exception {
        // Mock branchService behavior
        when(branchService.deleteBranch(1L)).thenReturn(false);

        // Perform DELETE request and verify results
        mockMvc.perform(delete("/api/v1/branches/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Branch Exception: Branch not found"))
                .andExpect(jsonPath("$.code").value(1100))
                .andExpect(jsonPath("$.content").isEmpty());

        // Verify branchService was called
        verify(branchService, times(1)).deleteBranch(1L);
    }
}