package com.zentrix.service;

import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.Staff;
import com.zentrix.model.entity.User;
import com.zentrix.model.entity.Warranty;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.WarrantyRequest;
import com.zentrix.repository.ProductTypeRepository;
import com.zentrix.repository.UserRepository;
import com.zentrix.repository.WarrantyRepository;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarrantyServiceImplTest {

    @Mock
    private WarrantyRepository warrantyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private StaffService staffService;

    @InjectMocks
    private WarrantyServiceImpl warrantyService;

    @BeforeEach
    void setUp() {
        reset(warrantyRepository, userRepository, productTypeRepository, staffService);
    }

    @Test
    void addWarranty_Success() {
        Long userId = 1L;
        Long prodTypeId = 1L;
        Long createdById = 1L;

        WarrantyRequest request = WarrantyRequest.builder()
                .userId(userId)
                .prodTypeId(prodTypeId)
                .createdBy(createdById)
                .warnStartDate(new Date())
                .warnEndDate(new Date(System.currentTimeMillis() + 86400000)) // +1 day
                .description("Warranty for product")
                .receive("Customer")
                .status(1)
                .build();

        User user = new User();
        user.setUserId(userId);

        ProductType productType = new ProductType();
        productType.setProdTypeId(prodTypeId);

        Staff staff = new Staff();
        staff.setStaffId(1L);

        Warranty savedWarranty = new Warranty();
        savedWarranty.setWarnId(1L);
        savedWarranty.setUserId(user);
        savedWarranty.setProdTypeId(productType);
        savedWarranty.setCreatedBy(staff);
        savedWarranty.setWarnStartDate(request.getWarnStartDate());
        savedWarranty.setWarnEndDate(request.getWarnEndDate());
        savedWarranty.setDescription(request.getDescription());
        savedWarranty.setReceive(request.getReceive());
        savedWarranty.setStatus(1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productTypeRepository.findById(prodTypeId)).thenReturn(Optional.of(productType));
        when(staffService.findStaffByUserId(createdById)).thenReturn(staff);
        when(warrantyRepository.save(any(Warranty.class))).thenReturn(savedWarranty);

        Warranty result = warrantyService.addWarranty(request, createdById);

        assertNotNull(result);
        assertEquals(1L, result.getWarnId());
        assertEquals(user, result.getUserId());
        assertEquals(productType, result.getProdTypeId());
        assertEquals(staff, result.getCreatedBy());
        assertEquals(request.getWarnStartDate(), result.getWarnStartDate());
        assertEquals(request.getWarnEndDate(), result.getWarnEndDate());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getReceive(), result.getReceive());
        assertEquals(1, result.getStatus());
        verify(userRepository).findById(userId);
        verify(productTypeRepository).findById(prodTypeId);
        verify(staffService).findStaffByUserId(createdById);
        verify(warrantyRepository).save(any(Warranty.class));
    }

    @Test
    void addWarranty_UserNotFound_ThrowsException() {
        WarrantyRequest request = WarrantyRequest.builder()
                .userId(1L)
                .prodTypeId(1L)
                .createdBy(1L)
                .warnStartDate(new Date())
                .warnEndDate(new Date(System.currentTimeMillis() + 86400000))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> warrantyService.addWarranty(request, 1L));

        assertEquals(AppCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
        verify(userRepository).findById(1L);
        verify(productTypeRepository, never()).findById(anyLong());
        verify(staffService, never()).findStaffByUserId(anyLong());
        verify(warrantyRepository, never()).save(any(Warranty.class));
    }

    @Test
    void findWarrantyById_Success() {
        Long warnId = 1L;
        Warranty warranty = new Warranty();
        warranty.setWarnId(warnId);

        when(warrantyRepository.findById(warnId)).thenReturn(Optional.of(warranty));

        Warranty result = warrantyService.findWarrantyById(warnId);

        assertNotNull(result);
        assertEquals(warnId, result.getWarnId());
        verify(warrantyRepository).findById(warnId);
    }

    @Test
    void findWarrantyById_NotFound_ReturnsNull() {
        Long warnId = 1L;

        when(warrantyRepository.findById(warnId)).thenReturn(Optional.empty());

        Warranty result = warrantyService.findWarrantyById(warnId);

        assertNull(result);
        verify(warrantyRepository).findById(warnId);
    }

    @Test
    void findAllWarranty_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Warranty> warranties = Arrays.asList(new Warranty(), new Warranty());
        Page<Warranty> warrantyPage = new PageImpl<>(warranties, pageable, warranties.size());

        when(warrantyRepository.findAll(pageable)).thenReturn(warrantyPage);

        Page<Warranty> result = warrantyService.findAllWarranty(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(warranties, result.getContent());
        verify(warrantyRepository).findAll(pageable);
    }

    @Test
    void updateWarranty_Success() {
        Long warnId = 1L;
        WarrantyRequest request = WarrantyRequest.builder()
                .warnEndDate(new Date(System.currentTimeMillis() + 86400000))
                .description("Updated warranty")
                .status(2)
                .build();

        Warranty existingWarranty = new Warranty();
        existingWarranty.setWarnId(warnId);
        existingWarranty.setWarnEndDate(new Date());
        existingWarranty.setDescription("Old description");
        existingWarranty.setStatus(1);

        Warranty updatedWarranty = new Warranty();
        updatedWarranty.setWarnId(warnId);
        updatedWarranty.setWarnEndDate(request.getWarnEndDate());
        updatedWarranty.setDescription(request.getDescription());
        updatedWarranty.setStatus(request.getStatus());

        when(warrantyRepository.findById(warnId)).thenReturn(Optional.of(existingWarranty));
        when(warrantyRepository.save(any(Warranty.class))).thenReturn(updatedWarranty);

        Warranty result = warrantyService.updateWarranty(request, warnId);

        assertNotNull(result);
        assertEquals(warnId, result.getWarnId());
        assertEquals(request.getWarnEndDate(), result.getWarnEndDate());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getStatus(), result.getStatus());
        verify(warrantyRepository).findById(warnId);
        verify(warrantyRepository).save(any(Warranty.class));
    }

    @Test
    void updateWarranty_NotFound_ThrowsException() {
        Long warnId = 1L;
        WarrantyRequest request = WarrantyRequest.builder()
                .warnEndDate(new Date())
                .status(2)
                .build();

        when(warrantyRepository.findById(warnId)).thenReturn(Optional.empty());

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> warrantyService.updateWarranty(request, warnId));

        assertEquals(AppCode.WARRANTY_NOT_FOUND.getMessage(), exception.getMessage());
        verify(warrantyRepository).findById(warnId);
        verify(warrantyRepository, never()).save(any(Warranty.class));
    }

    @Test
    void deleteWarranty_Success() {
        Long warnId = 1L;

        doNothing().when(warrantyRepository).deleteById(warnId);

        warrantyService.deleteWarranty(warnId);

        verify(warrantyRepository).deleteById(warnId);
    }

    @Test
    void findWarrantyByUserPhone_Success() {
        String phone = "1234567890";
        Pageable pageable = PageRequest.of(0, 10);
        List<Warranty> warranties = Arrays.asList(new Warranty(), new Warranty());
        Page<Warranty> warrantyPage = new PageImpl<>(warranties, pageable, warranties.size());

        when(warrantyRepository.findByUserPhone(phone, pageable)).thenReturn(warrantyPage);

        Page<Warranty> result = warrantyService.findWarrantyByUserPhone(phone, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(warranties, result.getContent());
        verify(warrantyRepository).findByUserPhone(phone, pageable);
    }
}