package com.zentrix.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.zentrix.model.entity.Promotion;
import com.zentrix.model.entity.Staff;
import com.zentrix.model.entity.User;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.PromotionRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.PromotionRepository;
import com.zentrix.repository.StaffRepository;
import com.zentrix.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class PromotionServiceImpl implements PromotionService {

    PromotionRepository promotionRepository;
    StaffRepository staffRepository;
    UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ActionFailedException(AppCode.USER_NOT_AUTHORIZED);
        }
        User user = userRepository.findUserByUsername(authentication.getName());
        if (user == null) {
            throw new ActionFailedException(AppCode.USER_NOT_FOUND);
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoleId() != null && "Admin".equalsIgnoreCase(user.getRoleId().getRoleName());
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Promotion updatePromotion(Long promId, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(promId)
                .orElseThrow(() -> new ActionFailedException(AppCode.PROMOTION_NOT_FOUND));

        Staff approveBy = null;
        User currentUser = getCurrentUser();

        if (isAdmin(currentUser)) {
            // Nếu là admin, luôn tự động gán approvedBy là admin hiện tại
            approveBy = staffRepository.findStaffByUserId(currentUser);
            if (approveBy == null) {
                throw new ActionFailedException(AppCode.STAFF_NOT_FOUND);
            }
        } else if (request.getApprovedBy() != null) {
            // Nếu không phải admin, chỉ dùng approvedBy từ request nếu có
            User approveByUser = userRepository.findById(request.getApprovedBy())
                    .orElseThrow(() -> new ActionFailedException(AppCode.USER_NOT_FOUND));
            approveBy = staffRepository.findStaffByUserId(approveByUser);
        }

        promotion.setApprovedBy(approveBy);
        promotion.setDiscount(request.getDiscount());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setQuantity(request.getQuantity());
        return promotionRepository.save(promotion);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Promotion createPromotion(PromotionRequest request) {
        Staff createdBy = null;
        Staff approvedBy = null;
        User currentUser = getCurrentUser();

        if (isAdmin(currentUser)) {
            // Nếu là admin, tự động gán createdBy và approvedBy là admin hiện tại
            createdBy = staffRepository.findStaffByUserId(currentUser);
            approvedBy = createdBy;
            if (createdBy == null) {
                throw new ActionFailedException(AppCode.STAFF_NOT_FOUND);
            }
        } else if (request.getCreatedBy() != null) {
            // Nếu không phải admin, sử dụng createdBy từ request
            User createdByUser = userRepository.findById(request.getCreatedBy())
                    .orElseThrow(() -> new ActionFailedException(AppCode.USER_NOT_FOUND));
            createdBy = staffRepository.findStaffByUserId(createdByUser);
            if (createdBy == null) {
                throw new ActionFailedException(AppCode.STAFF_NOT_FOUND);
            }
        }

        Promotion promotion = new Promotion();
        promotion.setCreatedBy(createdBy);
        promotion.setApprovedBy(approvedBy);
        promotion.setPromName(request.getPromName());
        promotion.setPromCode(request.getPromCode());
        promotion.setDiscount(request.getDiscount());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setQuantity(request.getQuantity());
        promotion.setPromStatus(1);

        try {
            return promotionRepository.save(promotion);
        } catch (Exception e) {
            throw new ActionFailedException(AppCode.PROMOTION_CREATION_FAILED);
        }
    }

    @Override
    public Promotion findPromotionById(Long promId) {
        return promotionRepository.findById(promId).orElse(null);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Promotion deletePromotion(Long promId) {
        Promotion promotion = promotionRepository.findById(promId)
                .orElseThrow(() -> new ActionFailedException(AppCode.PROMOTION_DELETE_FAILED));
        promotionRepository.deleteById(promId);
        return promotion;
    }

    @Override
    public PaginationWrapper<List<Promotion>> filterPromotions(String status, Date date, int page, int size,
            String sort) {
        try {
            Sort sortOrder = Sort.by(Sort.Direction.fromString(sort.split(",")[1]), sort.split(",")[0]);
            Pageable pageable = PageRequest.of(page, size, sortOrder);
            Page<Promotion> promotionPage;

            if (date != null) {
                promotionPage = promotionRepository.findActivePromotionsByDate(date, pageable);
            } else if ("active".equalsIgnoreCase(status)) {
                promotionPage = promotionRepository.findActivePromotions(pageable);
            } else if ("inactive".equalsIgnoreCase(status)) {
                promotionPage = promotionRepository.findInactivePromotions(pageable);
            } else {
                promotionPage = promotionRepository.findAll(pageable);
            }

            return new PaginationWrapper.Builder<List<Promotion>>()
                    .setData(promotionPage.getContent())
                    .setPaginationInfo(promotionPage)
                    .build();
        } catch (Exception e) {
            throw new ActionFailedException(AppCode.PROMOTION_GET_LIST_FAILED);
        }
    }

    @Override
    public PaginationWrapper<List<Promotion>> getPromotions(int page, int size, String sort) {
        return filterPromotions(null, null, page, size, sort);
    }

    @Override
    public PaginationWrapper<List<Promotion>> searchPromotion(String keyword, int page, int size, String sort) {
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sort.split(",")[1]), sort.split(",")[0]);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Long promId = null;
        try {
            promId = Long.parseLong(keyword);
        } catch (NumberFormatException e) {
            // Không xử lý lỗi ở đây
        }

        Page<Promotion> promotionPage = promotionRepository.findPromotion(keyword, promId, pageable);

        if (promotionPage.isEmpty()) {
            throw new ActionFailedException(AppCode.PROMOTION_NOT_FOUND);
        }

        return new PaginationWrapper.Builder<List<Promotion>>()
                .setData(promotionPage.getContent())
                .setPaginationInfo(promotionPage)
                .build();
    }

    @Override
    public boolean existsByPromCode(String promCode) {
        return promotionRepository.existsByPromCode(promCode);
    }
}