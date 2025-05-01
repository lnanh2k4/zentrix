package com.zentrix.controller;

import com.zentrix.model.entity.Variation;
import com.zentrix.model.request.VariationRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.VariationService;
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
class VariationControllerTest {

    @Mock
    private VariationService variationService;

    @InjectMocks
    private VariationController variationController;

    @BeforeEach
    void setUp() {
        reset(variationService);
    }

    // Test for getVariationById
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getVariationById_Success() {
        Long id = 1L;
        Variation variation = new Variation();

        when(variationService.getById(id)).thenReturn(variation);

        ResponseEntity<Variation> response = variationController.getVariationById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(variation, response.getBody());
        verify(variationService).getById(id);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getVariationById_NotFound() {
        Long id = 1L;

        when(variationService.getById(id)).thenReturn(null);

        ResponseEntity<Variation> response = variationController.getVariationById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(variationService).getById(id);
    }

    // Test for getAllVariations
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void getAllVariations_Success() {
        List<Variation> variations = Arrays.asList(new Variation());
        PaginationWrapper<List<Variation>> wrapper = new PaginationWrapper.Builder<List<Variation>>()
                .setData(variations)
                .setPage(0)
                .setSize(1000)
                .setTotalPages(1)
                .setTotalElements(variations.size())
                .build();

        when(variationService.getAll(0, 1000)).thenReturn(wrapper);

        ResponseEntity<ResponseObject<List<Variation>>> response = variationController.getAllVariations(0, 1000);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(variations, response.getBody().getContent());
        assertEquals("Get all variations successfully!", response.getBody().getMessage());
        assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
        verify(variationService).getAll(0, 1000);
    }

    // Test for createVariation
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createVariation_Success() {
        VariationRequest request = new VariationRequest();
        Variation variation = new Variation();

        when(variationService.create(request)).thenReturn(variation);

        ResponseEntity<Variation> response = variationController.createVariation(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(variation, response.getBody());
        verify(variationService).create(request);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void createVariation_Exception_ReturnsInternalServerError() {
        VariationRequest request = new VariationRequest();

        when(variationService.create(request)).thenThrow(new RuntimeException("Creation failed"));

        ResponseEntity<Variation> response = variationController.createVariation(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(variationService).create(request);
    }

    // Test for updateVariation
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateVariation_Success() {
        Long id = 1L;
        Variation variation = new Variation();
        Variation updatedVariation = new Variation();

        when(variationService.update(id, variation)).thenReturn(updatedVariation);

        ResponseEntity<Variation> response = variationController.updateVariation(id, variation);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedVariation, response.getBody());
        verify(variationService).update(id, variation);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void updateVariation_NotFound() {
        Long id = 1L;
        Variation variation = new Variation();

        when(variationService.update(id, variation)).thenReturn(null);

        ResponseEntity<Variation> response = variationController.updateVariation(id, variation);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(variationService).update(id, variation);
    }

    // Test for deleteVariation
    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteVariation_Success() {
        Long id = 1L;

        when(variationService.delete(id)).thenReturn(true);

        ResponseEntity<Void> response = variationController.deleteVariation(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(variationService).delete(id);
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE STAFF")
    void deleteVariation_NotFound() {
        Long id = 1L;

        when(variationService.delete(id)).thenReturn(false);

        ResponseEntity<Void> response = variationController.deleteVariation(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(variationService).delete(id);
    }
}