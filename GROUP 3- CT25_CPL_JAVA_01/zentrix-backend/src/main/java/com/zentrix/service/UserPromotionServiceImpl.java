package com.zentrix.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.zentrix.model.entity.Promotion;
import com.zentrix.model.entity.User;
import com.zentrix.model.entity.UserPromotion;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.UserPromotionRequest;
import com.zentrix.repository.PromotionRepository;
import com.zentrix.repository.UserPromotionRepository;
import com.zentrix.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPromotionServiceImpl implements UserPromotionService {

    private final UserPromotionRepository userPromotionRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public UserPromotion claimPromotion(UserPromotionRequest request, Long promId, Long userId) {
        if (promId == null) {
            throw new ActionFailedException(AppCode.PROMOTION_NOT_FOUND);
        }
        if (userId == null) {
            throw new ActionFailedException(AppCode.USER_NOT_FOUND);
        }

        Promotion promotion = promotionRepository.findById(promId)
                .orElseThrow(() -> new ActionFailedException(AppCode.PROMOTION_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ActionFailedException(AppCode.USER_NOT_FOUND));

        UserPromotion userPromotion = new UserPromotion();
        userPromotion.setPromId(promotion);
        userPromotion.setUserId(user);
        userPromotion.setStatus(1);

        return userPromotionRepository.save(userPromotion);
    }

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public UserPromotion addUserPromotion(UserPromotionRequest request, Long userPromId) {
        UserPromotion userPromotion = userPromotionRepository.findById(userPromId)
                .orElseThrow(() -> new ActionFailedException(AppCode.USER_PROMOTION_NOT_FOUND));
        userPromotion.setStatus(request.getStatus());

        return userPromotionRepository.save(userPromotion);
    }

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public void autoClaimUserPromotion(Long userId, Promotion promotionId) {
        UserPromotion userPromotion = UserPromotion.builder()
                .userId(User.builder().userId(userId).build())
                .promId(promotionId)
                .status(1)
                .build();
        userPromotionRepository.save(userPromotion);
    }

    @Override
    public List<UserPromotion> findAllUserPromotionByUserId(Long userId) {
        List<UserPromotion> userPromotions = userPromotionRepository.findAllByUserIdUserId(userId);
        return userPromotions;
    }
}