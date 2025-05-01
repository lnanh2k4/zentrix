package com.zentrix.controller;

import com.zentrix.model.entity.User;
import com.zentrix.model.entity.Warranty;
import com.zentrix.model.request.WarrantyRequest;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.EmailService;
import com.zentrix.service.UserService;
import com.zentrix.service.WarrantyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarrantyControllerTest {

    @Mock
    private WarrantyService warrantyService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @InjectMocks
    private WarrantyController warrantyController;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        reset(warrantyService, emailService, userService);
    }

    @Test
    @WithMockUser(roles = "SELLER STAFF")
    void updateWarranty_Success_StatusDone() {
        Long id = 1L;
        int status = 2;
        WarrantyRequest request = WarrantyRequest.builder()
                .warnEndDate(new Date())
                .description("Updated warranty")
                .status(status)
                .build();

        User user = new User();
        user.setUserId(1L);
        user.setEmail("user@example.com");

        Warranty existingWarranty = new Warranty();
        existingWarranty.setWarnId(id);
        existingWarranty.setUserId(user);

        Warranty updatedWarranty = new Warranty();
        updatedWarranty.setWarnId(id);
        updatedWarranty.setUserId(user);
        updatedWarranty.setWarnEndDate(request.getWarnEndDate());
        updatedWarranty.setDescription(request.getDescription());
        updatedWarranty.setStatus(status);

        when(warrantyService.findWarrantyById(id)).thenReturn(existingWarranty);
        when(warrantyService.updateWarranty(any(WarrantyRequest.class), eq(id))).thenReturn(updatedWarranty);
        when(userService.findUserByUserId(1L)).thenReturn(user);
        doNothing().when(emailService).sendEmailWithHtmlBody(anyString(), isNull(), isNull(), anyString(), anyString());

        ResponseEntity<ResponseObject<Warranty>> response = warrantyController.updateWarranty(request, id, status);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Warranty updated successfully", response.getBody().getMessage());
        assertEquals(updatedWarranty, response.getBody().getContent());
        verify(warrantyService).updateWarranty(any(WarrantyRequest.class), eq(id));
        verify(userService).findUserByUserId(1L);
        verify(emailService).sendEmailWithHtmlBody(eq("user@example.com"), isNull(), isNull(),
                eq("Warranty Completion"), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getWarrantyById_Success() {
        Long id = 1L;
        Warranty warranty = new Warranty();
        warranty.setWarnId(id);

        when(warrantyService.findWarrantyById(id)).thenReturn(warranty);

        ResponseEntity<ResponseObject<Warranty>> response = warrantyController.getWarrantyById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Warranty retrieved successfully", response.getBody().getMessage());
        assertEquals(warranty, response.getBody().getContent());
        verify(warrantyService).findWarrantyById(id);
    }

    @Test
    @WithMockUser(roles = "SELLER STAFF")
    void getWarrantyById_NotFound() {
        Long id = 1L;

        when(warrantyService.findWarrantyById(id)).thenReturn(null);

        ResponseEntity<ResponseObject<Warranty>> response = warrantyController.getWarrantyById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Warranty not found", response.getBody().getMessage());
        assertNull(response.getBody().getContent());
        verify(warrantyService).findWarrantyById(id);
    }

}