package com.zentrix.controller;

import com.zentrix.model.entity.Promotion;
import com.zentrix.model.entity.Role;
import com.zentrix.model.entity.User;
import com.zentrix.model.entity.UserPromotion;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.PromotionRequest;
import com.zentrix.model.request.UserPromotionRequest;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.repository.UserRepository;
import com.zentrix.service.PromotionService;
import com.zentrix.service.StaffService;
import com.zentrix.service.UserPromotionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromotionControllerTest {

    @Mock
    private PromotionService promotionService;

    @Mock
    private UserPromotionService userPromotionService;

    @Mock
    private StaffService staffService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PromotionController promotionController;

    @BeforeEach
    void setUp() {
        reset(promotionService, userPromotionService, staffService, userRepository, securityContext, authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // Helper method to set up authenticated user
    private void setupAuthenticatedUser(String username, String role, Long userId) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        User user = new User();
        user.setUserId(userId);
        Role roleObj = new Role();
        roleObj.setRoleName(role);
        user.setRoleId(roleObj);
        when(userRepository.findUserByUsername(username)).thenReturn(user);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPromotion_Admin_Success() {
        setupAuthenticatedUser("admin", "Admin", 1L);
        PromotionRequest request = new PromotionRequest();
        request.setPromName("Summer Sale");
        request.setPromCode("SUMMER2025");
        request.setDiscount(20.0f);
        request.setStartDate(new Date());
        request.setEndDate(new Date(System.currentTimeMillis() + 86400000)); // +1 day
        request.setQuantity(100);

        Promotion promotion = new Promotion();
        promotion.setPromId(1L);
        promotion.setPromName("Summer Sale");
        promotion.setPromCode("SUMMER2025");
        promotion.setDiscount(20.0f);
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setQuantity(100);
        promotion.setPromStatus(1);

        when(promotionService.createPromotion(any(PromotionRequest.class))).thenReturn(promotion);

        ResponseEntity<ResponseObject<Promotion>> response = promotionController.createPromotion(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(AppCode.PROMOTION_CREATION_SUCCESSFUL.getMessage(), response.getBody().getMessage());
        assertEquals(promotion, response.getBody().getContent());
        verify(promotionService).createPromotion(any(PromotionRequest.class));
    }

    @Test
    @WithMockUser(roles = "SELLER STAFF")
    void createPromotion_NonAdmin_StaffNotFound_ThrowsException() {
        setupAuthenticatedUser("staff", "SELLER STAFF", 2L);
        PromotionRequest request = new PromotionRequest();
        request.setPromName("Winter Sale");
        request.setCreatedBy(3L); // Non-admin sets createdBy

        when(staffService.findStaffByUserId(3L)).thenThrow(new ActionFailedException(AppCode.STAFF_NOT_FOUND));

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> promotionController.createPromotion(request));

        assertEquals("STAFF_NOT_FOUND", exception.getMessage());
        verify(staffService).findStaffByUserId(3L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePromotion_Admin_Success() {
        setupAuthenticatedUser("admin", "Admin", 1L);
        Long id = 1L;
        PromotionRequest request = new PromotionRequest();
        request.setPromName("Updated Summer Sale");
        request.setDiscount(25.0f);

        Promotion updatedPromotion = new Promotion();
        updatedPromotion.setPromId(id);
        updatedPromotion.setPromName("Updated Summer Sale");
        updatedPromotion.setDiscount(25.0f);
        updatedPromotion.setPromStatus(1);

        when(promotionService.updatePromotion(id, any(PromotionRequest.class))).thenReturn(updatedPromotion);

        ResponseEntity<ResponseObject<Promotion>> response = promotionController.updatePromotion(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Promotion updated successfully", response.getBody().getMessage());
        assertEquals(updatedPromotion, response.getBody().getContent());
        verify(promotionService).updatePromotion(id, any(PromotionRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePromotion_IdNull_ThrowsException() {
        setupAuthenticatedUser("admin", "Admin", 1L);
        PromotionRequest request = new PromotionRequest();
        request.setPromName("Invalid Update");

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> promotionController.updatePromotion(null, request));

        assertEquals("PROMOTION_NOT_FOUND", exception.getMessage());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void claimPromotion_Success() {
        setupAuthenticatedUser("customer", "CUSTOMER", 1L);
        Long promId = 1L;
        Long userId = 1L;

        // Create input request
        UserPromotionRequest request = new UserPromotionRequest();
        request.setPromId(promId);
        request.setUserId(userId);
        request.setStatus(1);

        // Create Promotion and User objects for UserPromotion
        Promotion promotion = new Promotion();
        promotion.setPromId(promId);
        promotion.setPromName("Summer Sale");
        promotion.setPromCode("SUMMER2025");

        User user = new User();
        user.setUserId(userId);
        user.setUsername("customer");
        Role role = new Role();
        role.setRoleName("CUSTOMER");
        user.setRoleId(role);

        // Create UserPromotion with object references
        UserPromotion userPromotion = new UserPromotion();
        userPromotion.setUserPromId(1L);
        userPromotion.setPromId(promotion); // Set Promotion object
        userPromotion.setUserId(user); // Set User object
        userPromotion.setStatus(1);

        // Mock service to return the UserPromotion
        when(userPromotionService.claimPromotion(any(UserPromotionRequest.class), eq(promId), eq(userId)))
                .thenReturn(userPromotion);

        // Call controller method
        ResponseEntity<ResponseObject<UserPromotion>> response = promotionController.claimPromotion(promId, userId);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Promotion claimed successfully", response.getBody().getMessage());
        assertEquals(userPromotion, response.getBody().getContent());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void claimPromotion_Failure_ReturnsBadRequest() {
        setupAuthenticatedUser("customer", "CUSTOMER", 1L);
        Long promId = 1L;
        Long userId = 1L;

        when(userPromotionService.claimPromotion(any(UserPromotionRequest.class), eq(promId), eq(userId)))
                .thenThrow(new RuntimeException("Insufficient quantity"));

        ResponseEntity<ResponseObject<UserPromotion>> response = promotionController.claimPromotion(promId, userId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to claim promotion: Insufficient quantity", response.getBody().getMessage());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getPromotionById_Found() {
        setupAuthenticatedUser("customer", "CUSTOMER", 1L);
        Long id = 1L;
        Promotion promotion = new Promotion();
        promotion.setPromId(id);
        promotion.setPromName("Summer Sale");
        promotion.setPromCode("SUMMER2025");

        when(promotionService.findPromotionById(id)).thenReturn(promotion);

        ResponseEntity<ResponseObject<Promotion>> response = promotionController.getPromotionById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Promotion found", response.getBody().getMessage());
        assertEquals(promotion, response.getBody().getContent());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getPromotionById_NotFound() {
        setupAuthenticatedUser("customer", "CUSTOMER", 1L);
        Long id = 1L;

        when(promotionService.findPromotionById(id)).thenReturn(null);

        ResponseEntity<ResponseObject<Promotion>> response = promotionController.getPromotionById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Promotion not found", response.getBody().getMessage());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void filterPromotions_InvalidCombination_ThrowsException() {
        setupAuthenticatedUser("customer", "CUSTOMER", 1L);
        String status = "active";
        Date date = new Date();

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> promotionController.filterPromotions(status, date, 0, 10, "promId,desc"));

        assertEquals("INVALID_FILTER_COMBINATION", exception.getMessage());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getUserPromotions_Failure_ReturnsBadRequest() {
        setupAuthenticatedUser("customer", "CUSTOMER", 1L);
        Long userId = 1L;

        when(userPromotionService.findAllUserPromotionByUserId(userId))
                .thenThrow(new RuntimeException("User not found"));

        ResponseEntity<ResponseObject<List<UserPromotion>>> response = promotionController.getUserPromotions(userId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to fetch user promotions: User not found", response.getBody().getMessage());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePromotion_Success() {
        setupAuthenticatedUser("admin", "Admin", 1L);
        Long id = 1L;
        Promotion deletedPromotion = new Promotion();
        deletedPromotion.setPromId(id);
        deletedPromotion.setPromName("Summer Sale");

        when(promotionService.deletePromotion(id)).thenReturn(deletedPromotion);

        ResponseEntity<ResponseObject<Promotion>> response = promotionController.deletePromotion(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Promotion deleted successfully", response.getBody().getMessage());
        assertEquals(deletedPromotion, response.getBody().getContent());
    }
}