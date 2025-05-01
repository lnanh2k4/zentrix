package com.zentrix.service;

import com.zentrix.model.entity.Promotion;
import com.zentrix.model.entity.Role;
import com.zentrix.model.entity.Staff;
import com.zentrix.model.entity.User;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.request.PromotionRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.PromotionRepository;
import com.zentrix.repository.StaffRepository;
import com.zentrix.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceImplTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PromotionServiceImpl promotionService;

    @BeforeEach
    void setUp() {
        reset(promotionRepository, staffRepository, userRepository, securityContext, authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // Helper method to set up authenticated user
    private void setupAuthenticatedUser(String username, boolean isAdmin) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        User user = new User();
        Role role = new Role();
        role.setRoleName(isAdmin ? "Admin" : "User");
        user.setRoleId(role);
        when(userRepository.findUserByUsername(username)).thenReturn(user);
    }

    // Test for createPromotion
    @Test
    void createPromotion_Admin_Success() {
        setupAuthenticatedUser("admin", true);
        PromotionRequest request = new PromotionRequest();
        request.setPromName("Test Promo");
        request.setPromCode("TEST123");
        request.setDiscount(10.0f);
        request.setStartDate(new Date());
        request.setEndDate(new Date());
        request.setQuantity(100);

        Staff staff = new Staff();
        Promotion savedPromotion = new Promotion();
        savedPromotion.setPromStatus(1);

        when(staffRepository.findStaffByUserId(any(User.class))).thenReturn(staff);
        when(promotionRepository.save(any(Promotion.class))).thenReturn(savedPromotion);

        Promotion result = promotionService.createPromotion(request);

        assertNotNull(result);
        assertEquals(1, result.getPromStatus());
        verify(staffRepository).findStaffByUserId(any(User.class));
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void createPromotion_NonAdmin_WithCreatedBy_Success() {
        setupAuthenticatedUser("user", false);
        PromotionRequest request = new PromotionRequest();
        request.setCreatedBy(1L);
        request.setPromName("Test Promo");

        User createdByUser = new User();
        Staff staff = new Staff();
        Promotion savedPromotion = new Promotion();

        when(userRepository.findById(1L)).thenReturn(Optional.of(createdByUser));
        when(staffRepository.findStaffByUserId(createdByUser)).thenReturn(staff);
        when(promotionRepository.save(any(Promotion.class))).thenReturn(savedPromotion);

        Promotion result = promotionService.createPromotion(request);

        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(staffRepository).findStaffByUserId(createdByUser);
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void createPromotion_UserNotAuthenticated_ThrowsActionFailedException() {
        when(securityContext.getAuthentication()).thenReturn(null);

        PromotionRequest request = new PromotionRequest();
        ActionFailedException exception = assertThrows(ActionFailedException.class, () -> {
            promotionService.createPromotion(request);
        });

        assertEquals("Post Validation: Created date cannot be in the future", exception.getMessage());
    }

    @Test
    void createPromotion_StaffNotFound_ThrowsActionFailedException() {
        setupAuthenticatedUser("admin", true);
        PromotionRequest request = new PromotionRequest();

        when(staffRepository.findStaffByUserId(any(User.class))).thenReturn(null);

        ActionFailedException exception = assertThrows(ActionFailedException.class, () -> {
            promotionService.createPromotion(request);
        });

        assertEquals("Staff Exception: Staff not found", exception.getMessage());
    }

    @Test
    void createPromotion_SaveFails_ThrowsActionFailedException() {
        setupAuthenticatedUser("admin", true);
        PromotionRequest request = new PromotionRequest();
        Staff staff = new Staff();

        when(staffRepository.findStaffByUserId(any(User.class))).thenReturn(staff);
        when(promotionRepository.save(any(Promotion.class))).thenThrow(new RuntimeException("DB Error"));

        ActionFailedException exception = assertThrows(ActionFailedException.class, () -> {
            promotionService.createPromotion(request);
        });

        assertEquals("Promotion Exception: Failed to create new promotion", exception.getMessage());
    }

    // Test for updatePromotion
    @Test
    void updatePromotion_Admin_Success() {
        setupAuthenticatedUser("admin", true);
        Long promId = 1L;
        PromotionRequest request = new PromotionRequest();
        request.setDiscount(15.0f);

        Promotion promotion = new Promotion();
        Staff staff = new Staff();

        when(promotionRepository.findById(promId)).thenReturn(Optional.of(promotion));
        when(staffRepository.findStaffByUserId(any(User.class))).thenReturn(staff);
        when(promotionRepository.save(promotion)).thenReturn(promotion);

        Promotion result = promotionService.updatePromotion(promId, request);

        assertNotNull(result);
        assertEquals(15.0f, result.getDiscount());
        verify(promotionRepository).save(promotion);
    }

    @Test
    void updatePromotion_NonAdmin_WithApprovedBy_Success() {
        setupAuthenticatedUser("user", false);
        Long promId = 1L;
        PromotionRequest request = new PromotionRequest();
        request.setApprovedBy(2L);

        Promotion promotion = new Promotion();
        User approveByUser = new User();
        Staff staff = new Staff();

        when(promotionRepository.findById(promId)).thenReturn(Optional.of(promotion));
        when(userRepository.findById(2L)).thenReturn(Optional.of(approveByUser));
        when(staffRepository.findStaffByUserId(approveByUser)).thenReturn(staff);
        when(promotionRepository.save(promotion)).thenReturn(promotion);

        Promotion result = promotionService.updatePromotion(promId, request);

        assertNotNull(result);
        verify(userRepository).findById(2L);
        verify(staffRepository).findStaffByUserId(approveByUser);
    }

    // Test for findPromotionById
    @Test
    void findPromotionById_Success() {
        Long promId = 1L;
        Promotion promotion = new Promotion();

        when(promotionRepository.findById(promId)).thenReturn(Optional.of(promotion));

        Promotion result = promotionService.findPromotionById(promId);

        assertNotNull(result);
        assertEquals(promotion, result);
    }

    @Test
    void findPromotionById_NotFound_ReturnsNull() {
        Long promId = 1L;

        when(promotionRepository.findById(promId)).thenReturn(Optional.empty());

        Promotion result = promotionService.findPromotionById(promId);

        assertNull(result);
    }

    // Test for deletePromotion
    @Test
    void deletePromotion_Success() {
        Long promId = 1L;
        Promotion promotion = new Promotion();

        when(promotionRepository.findById(promId)).thenReturn(Optional.of(promotion));
        doNothing().when(promotionRepository).deleteById(promId);

        Promotion result = promotionService.deletePromotion(promId);

        assertNotNull(result);
        assertEquals(promotion, result);
        verify(promotionRepository).deleteById(promId);
    }

    @Test
    void deletePromotion_NotFound_ThrowsActionFailedException() {
        Long promId = 1L;

        when(promotionRepository.findById(promId)).thenReturn(Optional.empty());

        ActionFailedException exception = assertThrows(ActionFailedException.class, () -> {
            promotionService.deletePromotion(promId);
        });

        assertEquals("Promotion Exception: Failed to delete promotion", exception.getMessage());
    }

    @Test
    void filterPromotions_RepositoryFails_ThrowsActionFailedException() {
        when(promotionRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("DB Error"));

        ActionFailedException exception = assertThrows(ActionFailedException.class, () -> {
            promotionService.filterPromotions(null, null, 0, 10, "promName,ASC");
        });

        assertEquals("Promotion Exception: Promotion get list failed", exception.getMessage());
    }

    // Test for getPromotions
    @Test
    void getPromotions_Success() {
        int page = 0;
        int size = 10;
        String sort = "promName,ASC";
        List<Promotion> promotions = Arrays.asList(new Promotion());
        Page<Promotion> promotionPage = new PageImpl<>(promotions);

        when(promotionRepository.findAll(any(Pageable.class))).thenReturn(promotionPage);

        PaginationWrapper<List<Promotion>> result = promotionService.getPromotions(page, size, sort);

        assertNotNull(result);
        assertEquals(promotions, result.getData());
    }

    // Test for existsByPromCode
    @Test
    void existsByPromCode_Success() {
        String promCode = "TEST123";

        when(promotionRepository.existsByPromCode(promCode)).thenReturn(true);

        boolean result = promotionService.existsByPromCode(promCode);

        assertTrue(result);
        verify(promotionRepository).existsByPromCode(promCode);
    }
}