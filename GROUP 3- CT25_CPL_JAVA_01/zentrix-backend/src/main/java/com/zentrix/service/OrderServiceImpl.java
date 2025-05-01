package com.zentrix.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.zentrix.model.entity.Branch;
import com.zentrix.model.entity.Order;
import com.zentrix.model.entity.Promotion;
import com.zentrix.model.entity.User;
import com.zentrix.model.entity.UserPromotion;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.OrderRequest;
import com.zentrix.model.request.PromotionRequest;
import com.zentrix.model.request.UserPromotionRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.OrderRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    UserService userService;
    PromotionService promotionService;
    BranchService branchService;
    UserPromotionService userPromotionService;

    @Override
    public PaginationWrapper<List<Order>> searchOrder(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.searchOrder(keyword, pageable);
        return new PaginationWrapper.Builder<List<Order>>()
                .setData(orderPage.getContent())
                .setPaginationInfo(orderPage)
                .build();
    }

    @Override
    public Order findOrderById(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        return order.orElse(null);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Order addOrder(OrderRequest orderRequest) {
        // Tạo đơn hàng
        Order order = new Order();
        User user = userService.findUserByUserId(orderRequest.getUserId());
        Promotion promotion = orderRequest.getPromId() != null
                ? promotionService.findPromotionById(orderRequest.getPromId())
                : null; // Chỉ lấy promotion nếu promId không null
        Branch branch = branchService.getBranchById(orderRequest.getBrchId());

        if (user == null) {
            throw new ActionFailedException(AppCode.USER_NOT_FOUND);
        }
        if (branch == null) {
            throw new ActionFailedException(AppCode.BRANCH_NOT_FOUND);
        }

        order.setUserId(user);
        order.setPromId(promotion); // Có thể null nếu không có promotion
        order.setBrchId(branch);
        order.setAddress(orderRequest.getAddress());
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setStatus(orderRequest.getStatus());
        order.setCreatedAt(java.time.LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Xử lý promotion nếu có
        if (promotion != null) { // Chỉ xử lý nếu promotion tồn tại
            // 1. Giảm quantity của promotion đi 1
            Integer currentQuantity = promotion.getQuantity();
            if (currentQuantity == null || currentQuantity <= 0) {
                throw new ActionFailedException(AppCode.PROMOTION_OUT_OF_STOCK);
            }

            // Tạo PromotionRequest để cập nhật quantity
            PromotionRequest promotionRequest = PromotionRequest.builder()
                    .createdBy(promotion.getCreatedBy() != null ? promotion.getCreatedBy().getStaffId() : null)
                    .approvedBy(promotion.getApprovedBy() != null ? promotion.getApprovedBy().getStaffId() : null)
                    .promName(promotion.getPromName())
                    .promCode(promotion.getPromCode())
                    .discount(promotion.getDiscount())
                    .startDate(promotion.getStartDate())
                    .endDate(promotion.getEndDate())
                    .quantity(currentQuantity - 1) // Giảm quantity đi 1
                    .build();

            promotionService.updatePromotion(orderRequest.getPromId(), promotionRequest);

            // 2. Cập nhật trạng thái UserPromotion thành 0
            List<UserPromotion> userPromotions = userPromotionService
                    .findAllUserPromotionByUserId(orderRequest.getUserId());
            UserPromotion targetUserPromotion = userPromotions.stream()
                    .filter(up -> up.getPromId().getPromId().equals(orderRequest.getPromId()) && up.getStatus() == 1)
                    .findFirst()
                    .orElse(null);

            if (targetUserPromotion != null) {
                UserPromotionRequest userPromotionRequest = UserPromotionRequest.builder()
                        .promId(targetUserPromotion.getPromId().getPromId())
                        .userId(targetUserPromotion.getUserId().getUserId())
                        .status(0) // Đặt status = 0 (hết hiệu lực)
                        .build();

                userPromotionService.addUserPromotion(userPromotionRequest, targetUserPromotion.getUserPromId());
            }
        }

        return savedOrder;
    }

    @Override
    public PaginationWrapper<List<Order>> findAllOrders(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orderPage = orderRepository.findAll(pageable);
            return new PaginationWrapper.Builder<List<Order>>()
                    .setData(orderPage.getContent())
                    .setPaginationInfo(orderPage)
                    .build();
        } catch (Exception e) {
            throw new ActionFailedException(AppCode.USER_GET_LIST_FAILED, e);
        }
    }

    @Override
    public PaginationWrapper<List<Order>> findOrdersByUser(User userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);

        if (orderPage.isEmpty()) {
            return new PaginationWrapper.Builder<List<Order>>()
                    .setData(List.of())
                    .setPaginationInfo(orderPage)
                    .build();
        }

        return new PaginationWrapper.Builder<List<Order>>()
                .setData(orderPage.getContent())
                .setPaginationInfo(orderPage)
                .build();
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Order updateOrder(Long orderId, OrderRequest orderRequest) {
        Order order = findOrderById(orderId);

        order.setStatus(orderRequest.getStatus());
        order.setPromId(order.getPromId());
        order.setBrchId(order.getBrchId());

        try {
            return orderRepository.save(order);
        } catch (Exception e) {
            throw new ActionFailedException(AppCode.ORDER_UPDATE_FAILED);
        }
    }
}