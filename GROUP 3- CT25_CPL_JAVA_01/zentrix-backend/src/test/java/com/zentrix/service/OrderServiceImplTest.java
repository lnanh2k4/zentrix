package com.zentrix.service;

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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date April 08, 2025
 */
@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private PromotionService promotionService;

    @Mock
    private BranchService branchService;

    @Mock
    private UserPromotionService userPromotionService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Branch branch;
    private Promotion promotion;
    private Order order;
    private OrderRequest orderRequest;
    private UserPromotion userPromotion;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);

        branch = new Branch();
        branch.setBrchId(1L);

        promotion = new Promotion();
        promotion.setPromId(1L);
        promotion.setQuantity(5);
        promotion.setPromName("Discount 10%");
        promotion.setPromCode("DISC10");
        promotion.setDiscount(10.0F);
        promotion.setStartDate(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        promotion.setEndDate(Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()));

        order = new Order();
        order.setOrderId(1L);
        order.setUserId(user);
        order.setBrchId(branch);
        order.setPromId(promotion);
        order.setAddress("123 Main St");
        order.setPaymentMethod("CREDIT_CARD");
        order.setStatus(1);
        order.setCreatedAt(LocalDateTime.now());

        orderRequest = new OrderRequest();
        orderRequest.setUserId(1L);
        orderRequest.setBrchId(1L);
        orderRequest.setPromId(1L);
        orderRequest.setAddress("123 Main St");
        orderRequest.setPaymentMethod("CREDIT_CARD");
        orderRequest.setStatus(1);

        userPromotion = new UserPromotion();
        userPromotion.setUserPromId(1L);
        userPromotion.setUserId(user);
        userPromotion.setPromId(promotion);
        userPromotion.setStatus(1);
    }

    @Test
    void testAddOrder_Success_WithPromotion() {
        when(userService.findUserByUserId(orderRequest.getUserId())).thenReturn(user);
        when(branchService.getBranchById(orderRequest.getBrchId())).thenReturn(branch);
        when(promotionService.findPromotionById(orderRequest.getPromId())).thenReturn(promotion);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(userPromotionService.findAllUserPromotionByUserId(orderRequest.getUserId()))
                .thenReturn(List.of(userPromotion));
        when(promotionService.updatePromotion(eq(orderRequest.getPromId()), any(PromotionRequest.class)))
                .thenReturn(promotion);
        when(userPromotionService.addUserPromotion(any(UserPromotionRequest.class), eq(userPromotion.getUserPromId())))
                .thenReturn(userPromotion);

        Order result = orderService.addOrder(orderRequest);

        assertNotNull(result);
        assertEquals(user, result.getUserId());
        assertEquals(branch, result.getBrchId());
        assertEquals(promotion, result.getPromId());
        assertEquals(orderRequest.getAddress(), result.getAddress());
        verify(userService, times(1)).findUserByUserId(orderRequest.getUserId());
        verify(branchService, times(1)).getBranchById(orderRequest.getBrchId());
        verify(promotionService, times(1)).findPromotionById(orderRequest.getPromId());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(userPromotionService, times(1)).findAllUserPromotionByUserId(orderRequest.getUserId());
        verify(promotionService, times(1)).updatePromotion(eq(orderRequest.getPromId()), any(PromotionRequest.class));
        verify(userPromotionService, times(1)).addUserPromotion(any(UserPromotionRequest.class),
                eq(userPromotion.getUserPromId()));
    }

    @Test
    void testAddOrder_Success_WithoutPromotion() {
        orderRequest.setPromId(null);

        when(userService.findUserByUserId(orderRequest.getUserId())).thenReturn(user);
        when(branchService.getBranchById(orderRequest.getBrchId())).thenReturn(branch);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.addOrder(orderRequest);

        assertNotNull(result);
        assertEquals(user, result.getUserId());
        assertEquals(branch, result.getBrchId());

        verify(userService, times(1)).findUserByUserId(orderRequest.getUserId());
        verify(branchService, times(1)).getBranchById(orderRequest.getBrchId());
        verify(promotionService, never()).findPromotionById(anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(userPromotionService, never()).findAllUserPromotionByUserId(anyLong());
    }

    @Test
    void testAddOrder_UserNotFound() {
        when(userService.findUserByUserId(orderRequest.getUserId())).thenReturn(null);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> orderService.addOrder(orderRequest));
        assertEquals(AppCode.USER_NOT_FOUND.getCode(), exception.getErrors().getCode());
        verify(userService, times(1)).findUserByUserId(orderRequest.getUserId());
    }

    @Test
    void testAddOrder_BranchNotFound() {
        when(userService.findUserByUserId(orderRequest.getUserId())).thenReturn(user);
        when(branchService.getBranchById(orderRequest.getBrchId())).thenReturn(null);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> orderService.addOrder(orderRequest));
        assertEquals(AppCode.BRANCH_NOT_FOUND.getCode(), exception.getErrors().getCode());
        verify(userService, times(1)).findUserByUserId(orderRequest.getUserId());
        verify(branchService, times(1)).getBranchById(orderRequest.getBrchId());
    }

    @Test
    void testAddOrder_PromotionOutOfStock() {
        promotion.setQuantity(0);

        when(userService.findUserByUserId(orderRequest.getUserId())).thenReturn(user);
        when(branchService.getBranchById(orderRequest.getBrchId())).thenReturn(branch);
        when(promotionService.findPromotionById(orderRequest.getPromId())).thenReturn(promotion);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> orderService.addOrder(orderRequest));
        assertEquals(AppCode.PROMOTION_OUT_OF_STOCK.getCode(), exception.getErrors().getCode());
        verify(userService, times(1)).findUserByUserId(orderRequest.getUserId());
        verify(branchService, times(1)).getBranchById(orderRequest.getBrchId());
        verify(promotionService, times(1)).findPromotionById(orderRequest.getPromId());
    }

    @Test
    void testFindAllOrders_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(page);

        PaginationWrapper<List<Order>> result = orderService.findAllOrders(0, 10);

        assertNotNull(result);
        assertFalse(result.getData().isEmpty());
        assertEquals(1, result.getData().size());
        assertEquals(order, result.getData().get(0));
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindAllOrders_Failure() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable)).thenThrow(new RuntimeException("Database error"));

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> orderService.findAllOrders(0, 10));
        assertEquals(AppCode.USER_GET_LIST_FAILED.getCode(), exception.getErrors().getCode());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindOrderById_Success() {
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order result = orderService.findOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void testFindOrderById_NotFound() {
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        Order result = orderService.findOrderById(orderId);

        assertNull(result);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void testFindOrdersByUser_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByUserId(user, pageable)).thenReturn(page);

        PaginationWrapper<List<Order>> result = orderService.findOrdersByUser(user, 0, 10);

        assertNotNull(result);
        assertFalse(result.getData().isEmpty());
        assertEquals(1, result.getData().size());
        assertEquals(order, result.getData().get(0));
        verify(orderRepository, times(1)).findByUserId(user, pageable);
    }

    @Test
    void testFindOrdersByUser_EmptyResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(orderRepository.findByUserId(user, pageable)).thenReturn(emptyPage);

        PaginationWrapper<List<Order>> result = orderService.findOrdersByUser(user, 0, 10);

        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
        verify(orderRepository, times(1)).findByUserId(user, pageable);
    }

    @Test
    void testSearchOrder_Success() {
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.searchOrder(keyword, pageable)).thenReturn(page);

        PaginationWrapper<List<Order>> result = orderService.searchOrder(keyword, 0, 10);

        assertNotNull(result);
        assertFalse(result.getData().isEmpty());
        assertEquals(1, result.getData().size());
        assertEquals(order, result.getData().get(0));
        verify(orderRepository, times(1)).searchOrder(keyword, pageable);
    }

    @Test
    void testUpdateOrder_Success() {
        Long orderId = 1L;
        orderRequest.setAddress("456 New St");
        orderRequest.setStatus(1);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.updateOrder(orderId, orderRequest);

        assertNotNull(result);
        assertEquals(orderRequest.getAddress(), result.getAddress());
        assertEquals(orderRequest.getStatus(), result.getStatus());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testUpdateOrder_Failure() {
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenThrow(new RuntimeException("Database error"));

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> orderService.updateOrder(orderId, orderRequest));
        assertEquals(AppCode.ORDER_UPDATE_FAILED.getCode(), exception.getErrors().getCode());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }
}