package com.zentrix.service;

import com.zentrix.model.entity.Attribute;
import com.zentrix.model.request.AttributeRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.AttributeRepository;
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
class AttributeServiceImplTest {

    @Mock
    private AttributeRepository repository;

    @InjectMocks
    private AttributeServiceImpl attributeService;

    @BeforeEach
    void setUp() {
        reset(repository);
    }

    // Test for getAll
    @Test
    void getAll_Success() {
        int page = 0;
        int size = 10;
        List<Attribute> attributes = Arrays.asList(new Attribute(), new Attribute());
        Page<Attribute> attributePage = new PageImpl<>(attributes, PageRequest.of(page, size), attributes.size());

        when(repository.findAll(any(Pageable.class))).thenReturn(attributePage);

        PaginationWrapper<List<Attribute>> result = attributeService.getAll(page, size);

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
        Attribute attribute = new Attribute();

        when(repository.findById(id)).thenReturn(Optional.of(attribute));

        Attribute result = attributeService.getById(id);

        assertNotNull(result);
        assertEquals(attribute, result);
        verify(repository).findById(id);
    }

    @Test
    void getById_NotFound() {
        Long id = 1L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        Attribute result = attributeService.getById(id);

        assertNull(result);
        verify(repository).findById(id);
    }

    // Test for create
    @Test
    void create_Success() {
        AttributeRequest request = new AttributeRequest();
        request.setAtbName("Color");
        Attribute createdAttribute = new Attribute();
        createdAttribute.setAtbName("Color");

        when(repository.save(any(Attribute.class))).thenReturn(createdAttribute);

        Attribute result = attributeService.create(request);

        assertNotNull(result);
        assertEquals("Color", result.getAtbName());
        verify(repository).save(any(Attribute.class));
    }

    @Test
    void create_Exception_ThrowsRuntimeException() {
        AttributeRequest request = new AttributeRequest();
        request.setAtbName("Color");

        when(repository.save(any(Attribute.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            attributeService.create(request);
        });

        verify(repository).save(any(Attribute.class));
    }

    // Test for update
    @Test
    void update_Success() {
        Long id = 1L;
        Attribute attribute = new Attribute();
        attribute.setAtbName("Size");
        Attribute updatedAttribute = new Attribute();
        updatedAttribute.setAtbId(id);
        updatedAttribute.setAtbName("Size");

        when(repository.existsById(id)).thenReturn(true);
        when(repository.save(any(Attribute.class))).thenReturn(updatedAttribute);

        Attribute result = attributeService.update(id, attribute);

        assertNotNull(result);
        assertEquals(id, result.getAtbId());
        assertEquals("Size", result.getAtbName());
        verify(repository).existsById(id);
        verify(repository).save(any(Attribute.class));
    }

    @Test
    void update_NotFound() {
        Long id = 1L;
        Attribute attribute = new Attribute();

        when(repository.existsById(id)).thenReturn(false);

        Attribute result = attributeService.update(id, attribute);

        assertNull(result);
        verify(repository).existsById(id);
        verify(repository, never()).save(any(Attribute.class));
    }

    @Test
    void update_Exception_ThrowsRuntimeException() {
        Long id = 1L;
        Attribute attribute = new Attribute();

        when(repository.existsById(id)).thenReturn(true);
        when(repository.save(any(Attribute.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            attributeService.update(id, attribute);
        });

        verify(repository).existsById(id);
        verify(repository).save(any(Attribute.class));
    }

    // Test for delete
    @Test
    void delete_Success() {
        Long id = 1L;

        doNothing().when(repository).deleteById(id);

        boolean result = attributeService.delete(id);

        assertTrue(result);
        verify(repository).deleteById(id);
    }

    @Test
    void delete_Exception_ThrowsRuntimeException() {
        Long id = 1L;

        doThrow(new RuntimeException("Database error")).when(repository).deleteById(id);

        assertThrows(RuntimeException.class, () -> {
            attributeService.delete(id);
        });

        verify(repository).deleteById(id);
    }
}