package com.zentrix.service;

import com.zentrix.model.entity.Promotion;
import com.zentrix.model.entity.User;
import com.zentrix.model.entity.UserPromotion;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.UserPromotionRequest;
import com.zentrix.repository.PromotionRepository;
import com.zentrix.repository.UserPromotionRepository;
import com.zentrix.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserPromotionServiceImplTest {

    @Mock
    private UserPromotionRepository userPromotionRepository;

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserPromotionServiceImpl userPromotionService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        reset(userPromotionRepository, promotionRepository, userRepository);
    }

    @Test
    void claimPromotion_Success() {
        Long promId = 1L;
        Long userId = 1L;

        UserPromotionRequest request = new UserPromotionRequest(promId, userId, 1);

        Promotion promotion = new Promotion();
        promotion.setPromId(promId);
        promotion.setPromName("Summer Sale");

        User user = new User();
        user.setUserId(userId);
        user.setUsername("customer");

        UserPromotion savedUserPromotion = new UserPromotion();
        savedUserPromotion.setUserPromId(1L);
        savedUserPromotion.setPromId(promotion);
        savedUserPromotion.setUserId(user);
        savedUserPromotion.setStatus(1);

        when(promotionRepository.findById(promId)).thenReturn(Optional.of(promotion));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userPromotionRepository.save(any(UserPromotion.class))).thenReturn(savedUserPromotion);

        UserPromotion result = userPromotionService.claimPromotion(request, promId, userId);

        assertNotNull(result);
        assertEquals(1L, result.getUserPromId());
        assertEquals(promotion, result.getPromId());
        assertEquals(user, result.getUserId());
        assertEquals(1, result.getStatus());
        verify(promotionRepository).findById(promId);
        verify(userRepository).findById(userId);
        verify(userPromotionRepository).save(any(UserPromotion.class));
    }

    @Test
    void claimPromotion_PromIdNull_ThrowsException() {
        Long userId = 1L;
        UserPromotionRequest request = new UserPromotionRequest(null, userId, 1);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> userPromotionService.claimPromotion(request, null, userId));

        assertEquals(AppCode.PROMOTION_NOT_FOUND.getMessage(), exception.getMessage());
        verify(promotionRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
        verify(userPromotionRepository, never()).save(any(UserPromotion.class));
    }

    @Test
    void claimPromotion_UserIdNull_ThrowsException() {
        Long promId = 1L;
        UserPromotionRequest request = new UserPromotionRequest(promId, null, 1);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> userPromotionService.claimPromotion(request, promId, null));

        assertEquals(AppCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
        verify(promotionRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
        verify(userPromotionRepository, never()).save(any(UserPromotion.class));
    }

    @Test
    void claimPromotion_PromotionNotFound_ThrowsException() {
        Long promId = 1L;
        Long userId = 1L;
        UserPromotionRequest request = new UserPromotionRequest(promId, userId, 1);

        when(promotionRepository.findById(promId)).thenReturn(Optional.empty());

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> userPromotionService.claimPromotion(request, promId, userId));

        assertEquals(AppCode.PROMOTION_NOT_FOUND.getMessage(), exception.getMessage());
        verify(promotionRepository).findById(promId);
        verify(userRepository, never()).findById(anyLong());
        verify(userPromotionRepository, never()).save(any(UserPromotion.class));
    }

    @Test
    void claimPromotion_UserNotFound_ThrowsException() {
        Long promId = 1L;
        Long userId = 1L;
        UserPromotionRequest request = new UserPromotionRequest(promId, userId, 1);

        Promotion promotion = new Promotion();
        promotion.setPromId(promId);

        when(promotionRepository.findById(promId)).thenReturn(Optional.of(promotion));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> userPromotionService.claimPromotion(request, promId, userId));

        assertEquals(AppCode.USER_NOT_FOUND.getMessage(), exception.getMessage());
        verify(promotionRepository).findById(promId);
        verify(userRepository).findById(userId);
        verify(userPromotionRepository, never()).save(any(UserPromotion.class));
    }

    @Test
    void addUserPromotion_Success() {
        Long userPromId = 1L;
        UserPromotionRequest request = new UserPromotionRequest(1L, 1L, 2);

        Promotion promotion = new Promotion();
        promotion.setPromId(1L);

        User user = new User();
        user.setUserId(1L);

        UserPromotion existingUserPromotion = new UserPromotion();
        existingUserPromotion.setUserPromId(userPromId);
        existingUserPromotion.setPromId(promotion);
        existingUserPromotion.setUserId(user);
        existingUserPromotion.setStatus(1);

        UserPromotion updatedUserPromotion = new UserPromotion();
        updatedUserPromotion.setUserPromId(userPromId);
        updatedUserPromotion.setPromId(promotion);
        updatedUserPromotion.setUserId(user);
        updatedUserPromotion.setStatus(2);

        when(userPromotionRepository.findById(userPromId)).thenReturn(Optional.of(existingUserPromotion));
        when(userPromotionRepository.save(any(UserPromotion.class))).thenReturn(updatedUserPromotion);

        UserPromotion result = userPromotionService.addUserPromotion(request, userPromId);

        assertNotNull(result);
        assertEquals(userPromId, result.getUserPromId());
        assertEquals(promotion, result.getPromId());
        assertEquals(user, result.getUserId());
        assertEquals(2, result.getStatus());
        verify(userPromotionRepository).findById(userPromId);
        verify(userPromotionRepository).save(any(UserPromotion.class));
    }

    @Test
    void addUserPromotion_NotFound_ThrowsException() {
        Long userPromId = 1L;
        UserPromotionRequest request = new UserPromotionRequest(1L, 1L, 2);

        when(userPromotionRepository.findById(userPromId)).thenReturn(Optional.empty());

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> userPromotionService.addUserPromotion(request, userPromId));

        assertEquals(AppCode.USER_PROMOTION_NOT_FOUND.getMessage(), exception.getMessage());
        verify(userPromotionRepository).findById(userPromId);
        verify(userPromotionRepository, never()).save(any(UserPromotion.class));
    }

    @Test
    void autoClaimUserPromotion_Success() {
        Long userId = 1L;
        Promotion promotion = new Promotion();
        promotion.setPromId(1L);
        promotion.setPromName("Auto Claim Promo");

        UserPromotion savedUserPromotion = new UserPromotion();
        savedUserPromotion.setUserPromId(1L);
        savedUserPromotion.setPromId(promotion);
        savedUserPromotion.setUserId(new User());
        savedUserPromotion.getUserId().setUserId(userId);
        savedUserPromotion.setStatus(1);

        when(userPromotionRepository.save(any(UserPromotion.class))).thenReturn(savedUserPromotion);

        userPromotionService.autoClaimUserPromotion(userId, promotion);

        verify(userPromotionRepository).save(any(UserPromotion.class));
    }

    @Test
    void findAllUserPromotionByUserId_Success() {
        Long userId = 1L;

        Promotion promotion1 = new Promotion();
        promotion1.setPromId(1L);
        Promotion promotion2 = new Promotion();
        promotion2.setPromId(2L);

        User user = new User();
        user.setUserId(userId);

        List<UserPromotion> userPromotions = Arrays.asList(
                new UserPromotion(1L, promotion1, user, 1),
                new UserPromotion(2L, promotion2, user, 1));

        when(userPromotionRepository.findAllByUserIdUserId(userId)).thenReturn(userPromotions);

        List<UserPromotion> result = userPromotionService.findAllUserPromotionByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getUserPromId());
        assertEquals(promotion1, result.get(0).getPromId());
        assertEquals(user, result.get(0).getUserId());
        assertEquals(1, result.get(0).getStatus());
        assertEquals(2L, result.get(1).getUserPromId());
        verify(userPromotionRepository).findAllByUserIdUserId(userId);
    }

    @Test
    void findAllUserPromotionByUserId_EmptyList() {
        Long userId = 1L;

        when(userPromotionRepository.findAllByUserIdUserId(userId)).thenReturn(Arrays.asList());

        List<UserPromotion> result = userPromotionService.findAllUserPromotionByUserId(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userPromotionRepository).findAllByUserIdUserId(userId);
    }
}