package com.zentrix.service;

import com.zentrix.model.entity.Variation;
import com.zentrix.model.request.VariationRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.VariationRepository;
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
class VariationServiceImplTest {

    @Mock
    private VariationRepository repository;

    @InjectMocks
    private VariationServiceImpl variationService;

    @BeforeEach
    void setUp() {
        reset(repository);
    }

    // Test for getAll
    @Test
    void getAll_Success() {
        int page = 0;
        int size = 10;
        List<Variation> variations = Arrays.asList(new Variation(), new Variation());
        Page<Variation> variationPage = new PageImpl<>(variations, PageRequest.of(page, size), variations.size());

        when(repository.findAll(any(Pageable.class))).thenReturn(variationPage);

        PaginationWrapper<List<Variation>> result = variationService.getAll(page, size);

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
        Variation variation = new Variation();

        when(repository.findById(id)).thenReturn(Optional.of(variation));

        Variation result = variationService.getById(id);

        assertNotNull(result);
        assertEquals(variation, result);
        verify(repository).findById(id);
    }

    @Test
    void getById_NotFound() {
        Long id = 1L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        Variation result = variationService.getById(id);

        assertNull(result);
        verify(repository).findById(id);
    }

    // Test for create
    @Test
    void create_Success() {
        VariationRequest request = new VariationRequest();
        request.setVariName("Size");
        Variation createdVariation = new Variation();
        createdVariation.setVariName("Size");

        when(repository.save(any(Variation.class))).thenReturn(createdVariation);

        Variation result = variationService.create(request);

        assertNotNull(result);
        assertEquals("Size", result.getVariName());
        verify(repository).save(any(Variation.class));
    }

    @Test
    void create_Exception_ThrowsRuntimeException() {
        VariationRequest request = new VariationRequest();
        request.setVariName("Size");

        when(repository.save(any(Variation.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            variationService.create(request);
        });

        verify(repository).save(any(Variation.class));
    }

    // Test for update
    @Test
    void update_Success() {
        Long id = 1L;
        Variation variation = new Variation();
        variation.setVariName("Color");
        Variation updatedVariation = new Variation();
        updatedVariation.setVariId(id);
        updatedVariation.setVariName("Color");

        when(repository.existsById(id)).thenReturn(true);
        when(repository.save(any(Variation.class))).thenReturn(updatedVariation);

        Variation result = variationService.update(id, variation);

        assertNotNull(result);
        assertEquals(id, result.getVariId());
        assertEquals("Color", result.getVariName());
        verify(repository).existsById(id);
        verify(repository).save(any(Variation.class));
    }

    @Test
    void update_NotFound() {
        Long id = 1L;
        Variation variation = new Variation();

        when(repository.existsById(id)).thenReturn(false);

        Variation result = variationService.update(id, variation);

        assertNull(result);
        verify(repository).existsById(id);
        verify(repository, never()).save(any(Variation.class));
    }

    @Test
    void update_Exception_ThrowsRuntimeException() {
        Long id = 1L;
        Variation variation = new Variation();

        when(repository.existsById(id)).thenReturn(true);
        when(repository.save(any(Variation.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            variationService.update(id, variation);
        });

        verify(repository).existsById(id);
        verify(repository).save(any(Variation.class));
    }

    // Test for delete
    @Test
    void delete_Success() {
        Long id = 1L;

        doNothing().when(repository).deleteById(id);

        boolean result = variationService.delete(id);

        assertTrue(result);
        verify(repository).deleteById(id);
    }

    @Test
    void delete_Exception_ThrowsRuntimeException() {
        Long id = 1L;

        doThrow(new RuntimeException("Database error")).when(repository).deleteById(id);

        assertThrows(RuntimeException.class, () -> {
            variationService.delete(id);
        });

        verify(repository).deleteById(id);
    }
}