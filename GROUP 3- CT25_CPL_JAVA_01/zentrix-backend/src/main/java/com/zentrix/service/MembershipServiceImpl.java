package com.zentrix.service;

import com.zentrix.model.entity.Membership;
import com.zentrix.model.entity.Promotion;
import com.zentrix.model.entity.User;
import com.zentrix.model.entity.UserPromotion;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.model.request.MembershipRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.MembershipRepository;
import com.zentrix.repository.PromotionRepository;
import com.zentrix.repository.UserPromotionRepository;
import com.zentrix.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
* @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MembershipServiceImpl implements MembershipService {

    MembershipRepository membershipRepository;
    PromotionRepository promotionRepository;
    UserPromotionRepository userPromotionRepository;
    UserRepository userRepository;
    PromotionGenerator promotionGenerator;
    UserPromotionService userPromotionService;

    @Override

    public PaginationWrapper<List<Membership>> getAllMemberships(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Membership> membershipPage = membershipRepository.findAll(pageable);
            return new PaginationWrapper.Builder<List<Membership>>()
                    .setData(membershipPage.getContent())
                    .setPaginationInfo(
                            membershipPage)
                    .build();
        } catch (Exception e) {
            throw new ActionFailedException(AppCode.USER_GET_LIST_FAILED, e);
        }
    }

    @Override
    public Membership getMembershipById(Long id) {
        Optional<Membership> membership = membershipRepository.findById(id);
        return membership.orElse(null);
    }

    private boolean existsByMbsName(String mbsName, Long excludeId) {
        Optional<Membership> existingMembership = membershipRepository.findByMbsNameIgnoreCase(mbsName);
        return existingMembership.isPresent()
                && (excludeId == null || !existingMembership.get().getMbsId().equals(excludeId));
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Membership createMembership(MembershipRequest req) {
        if (req == null || req.getMbsName() == null || req.getMbsDescription() == null) {
            throw new ActionFailedException(AppCode.MEMBERSHIP_CREATION_FAILED);
        }

        // Check for duplicate mbsName
        if (existsByMbsName(req.getMbsName(), null)) {
            throw new ActionFailedException(AppCode.MEMBERSHIP_CREATION_FAILED);
        }

        Membership membership = new Membership();
        membership.setMbsName(req.getMbsName());
        membership.setMbsPoint(req.getMbsPoint());
        membership.setMbsDescription(req.getMbsDescription());
        return membershipRepository.save(membership);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Membership saveMembership(Membership membership) {
        if (membership == null || membership.getMbsId() == null) {
            throw new ActionFailedException(AppCode.MEMBERSHIP_UPDATE_FAILED);
        }

        // Check for duplicate mbsName, excluding the current membership
        if (existsByMbsName(membership.getMbsName(), membership.getMbsId())) {
            throw new ActionFailedException(AppCode.MEMBERSHIP_UPDATE_FAILED);
        }

        return membershipRepository.save(membership);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void deleteMembership(Long id) {

        Optional<Membership> membership = membershipRepository.findById(id);

        if (membership.isEmpty()) {
            throw new ActionFailedException(AppCode.MEMBERSHIP_DELETE_FAILED);
        }

        membershipRepository.deleteById(id);
    }

    @Override

    public PaginationWrapper<List<Membership>> findMembershipByName(String mbsName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Membership> membershipPage = membershipRepository.findByMbsName(mbsName, pageable);

        if (membershipPage.isEmpty()) {
            throw new ValidationFailedException(AppCode.USER_NOT_FOUND);

        }
        return new PaginationWrapper.Builder<List<Membership>>()
                .setData(membershipPage.getContent())
                .setPaginationInfo(
                        membershipPage)
                .build();
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Membership updateMembership(Long id, MembershipRequest req) {

        if (req == null || req.getMbsName() == null || req.getMbsDescription() == null) {
            throw new ActionFailedException(AppCode.MEMBERSHIP_UPDATE_FAILED);
        }

        Membership existingMembership = membershipRepository.findById(id)
                .orElseThrow(() -> new ActionFailedException(AppCode.MEMBERSHIP_NOT_FOUND));

        existingMembership.setMbsName(req.getMbsName());
        existingMembership.setMbsPoint(req.getMbsPoint());

        existingMembership.setMbsDescription(req.getMbsDescription());
        return membershipRepository.save(existingMembership);
    }

    @Transactional(rollbackFor = { ActionFailedException.class })
    public void assignPromotionOnRankUp(Long userId, Long mbsId, String username) {
        Membership membership = membershipRepository.findById(mbsId)
                .orElseThrow(() -> new ActionFailedException(AppCode.MEMBERSHIP_NOT_FOUND));

        Promotion promotion = createPromotionFromMembership(membership, username);
        saveUserPromotion(userId, promotion);
    }

    private Promotion createPromotionFromMembership(Membership membership, String username) {
        PromotionDetails details = new PromotionDetails(membership.getMbsName(), membership.getMbsDescription());
        String promCode = details.promCodeBase + "_" + username;

        Promotion existingPromotion = promotionRepository.findByPromCode(promCode).orElse(null);
        if (existingPromotion != null) {
            return existingPromotion;
        }

        Promotion promotion = Promotion.builder()
                .promName(details.promName)
                .promCode(promCode)
                .discount((float) details.discount)
                .startDate(new Date())
                .endDate(new Date(System.currentTimeMillis() + details.duration * 24L * 60 * 60 * 1000))
                .quantity(1)
                .promStatus(1)
                .build();

        return promotionRepository.save(promotion);
    }

    private void saveUserPromotion(Long userId, Promotion promotion) {
        UserPromotion userPromotion = UserPromotion.builder()
                .userId(User.builder().userId(userId).build())
                .promId(promotion)
                .status(1)
                .build();
        userPromotionRepository.save(userPromotion);
    }

    public List<Promotion> getUserPromotions(Long userId) {
        List<UserPromotion> userPromotions = userPromotionRepository.findAllByUserIdUserId(userId);
        return userPromotions.stream()
                .map(up -> promotionRepository.findById(up.getPromId().getPromId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static class PromotionDetails {
        String promName;
        String promCodeBase;
        int duration;
        double discount;

        PromotionDetails(String rankName, String description) {
            this.promName = rankName + " Membership Reward";
            Pattern pattern = Pattern.compile("valid for (\\d+) (?:month|day)s?.*offering a direct (\\d+)k discount");
            Matcher matcher = pattern.matcher(description);
            if (matcher.find()) {
                this.duration = Integer.parseInt(matcher.group(1)) * (description.contains("month") ? 30 : 1);
                this.discount = Double.parseDouble(matcher.group(2)) / 1000.0; // Chuyển từ k sang triệu đồng
                this.promCodeBase = rankName.toUpperCase() + matcher.group(2) + "K";
            } else {
                this.duration = 30;
                this.discount = 0.05;
                this.promCodeBase = rankName.toUpperCase() + "DEFAULT";
            }
        }
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void applyMembershipToUser(Long userId, Long mbsId) {
        Membership membership = membershipRepository.findById(mbsId)
                .orElseThrow(() -> new ActionFailedException(AppCode.MEMBERSHIP_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ActionFailedException(AppCode.USER_NOT_FOUND));

        Integer currentPoints = user.getUserPoint() != null ? user.getUserPoint() : 0;
        user.setUserPoint(currentPoints + membership.getMbsPoint().intValue());

        user.setMbsId(membership);

        userRepository.save(user);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void autoUpdateMembership(Long userId, Long accumulatedPoints) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ActionFailedException(AppCode.USER_NOT_FOUND));

        List<Membership> memberships = membershipRepository.findAll().stream()
                .sorted((a, b) -> a.getMbsPoint().compareTo(b.getMbsPoint()))
                .collect(Collectors.toList());

        Membership currentMembership = user.getMbsId();
        Membership highestEligibleMembership = null;

        for (Membership membership : memberships) {
            if (accumulatedPoints >= membership.getMbsPoint()) {
                highestEligibleMembership = membership;
            } else {
                break;
            }
        }

        user.setUserPoint(accumulatedPoints.intValue());

        if (highestEligibleMembership != null) {
            boolean isRankUp = currentMembership == null ||
                    !currentMembership.getMbsId().equals(highestEligibleMembership.getMbsId());

            user.setMbsId(highestEligibleMembership);

            if (isRankUp) {
                promotionGenerator.generateAndAssignPromotions(userId, highestEligibleMembership, user.getUsername());
            }
        }

        userRepository.save(user);
    }
}
