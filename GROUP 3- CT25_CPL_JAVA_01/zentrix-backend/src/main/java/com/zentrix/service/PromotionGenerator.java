package com.zentrix.service;

import com.zentrix.model.entity.Membership;
import com.zentrix.model.entity.Promotion;
import com.zentrix.repository.PromotionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionGenerator {

    PromotionRepository promotionRepository;
    UserPromotionService userPromotionService;

    @Transactional(rollbackFor = Exception.class)
    public void generateAndAssignPromotions(Long userId, Membership membership, String username) {
        List<PromotionDetails> promotionDetailsList = parsePromotionDetails(membership.getMbsDescription());

        for (PromotionDetails details : promotionDetailsList) {
            String promCode = membership.getMbsName().toUpperCase() + "_" + username +
                    (promotionDetailsList.size() > 1 ? "_" + details.index : "");
            Promotion existingPromotion = promotionRepository.findByPromCode(promCode).orElse(null);

            if (existingPromotion == null) {
                Promotion promotion = Promotion.builder()
                        .promName(membership.getMbsName() + " Promotion")
                        .promCode(promCode)
                        .discount(details.discount)
                        .startDate(new Date())
                        .endDate(calculateEndDate(new Date(), details.duration))
                        .quantity(details.quantity)
                        .promStatus(1)
                        .build();
                promotion = promotionRepository.save(promotion);
                userPromotionService.autoClaimUserPromotion(userId, promotion);
            } else {
                userPromotionService.autoClaimUserPromotion(userId, existingPromotion);
            }
        }
    }

    private Date calculateEndDate(Date startDate, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    private List<PromotionDetails> parsePromotionDetails(String description) {
        List<PromotionDetails> detailsList = new ArrayList<>();

        Pattern quantityPattern = Pattern.compile("Provides (\\d+) vouchers?");
        Pattern durationPattern = Pattern.compile("valid for (\\d+) months?");
        Pattern percentDiscountPattern = Pattern.compile("(\\d+)% discount");

        Matcher quantityMatcher = quantityPattern.matcher(description);
        Matcher durationMatcher = durationPattern.matcher(description);
        Matcher percentDiscountMatcher = percentDiscountPattern.matcher(description);

        int quantity = 1;
        int duration = 1;
        float discount = 5f;
        int index = 1;

        if (quantityMatcher.find()) {
            quantity = Integer.parseInt(quantityMatcher.group(1));
        }

        if (durationMatcher.find()) {
            duration = Integer.parseInt(durationMatcher.group(1));
        }

        if (percentDiscountMatcher.find()) {
            discount = Float.parseFloat(percentDiscountMatcher.group(1));
        }

        detailsList.add(new PromotionDetails(1, duration, discount, index++));

        if (quantity > 1) {
            for (int i = 1; i < quantity; i++) {
                detailsList.add(new PromotionDetails(1, duration, discount, index++));
            }
        }

        if (detailsList.isEmpty()) {
            detailsList.add(new PromotionDetails(1, 1, 5f, 1));
        }

        return detailsList;
    }

    private static class PromotionDetails {
        int quantity;
        int duration; // Số tháng
        float discount; // Phần trăm
        int index; // Để phân biệt khi có nhiều voucher

        PromotionDetails(int quantity, int duration, float discount, int index) {
            this.quantity = quantity;
            this.duration = duration;
            this.discount = discount;
            this.index = index;
        }
    }
}