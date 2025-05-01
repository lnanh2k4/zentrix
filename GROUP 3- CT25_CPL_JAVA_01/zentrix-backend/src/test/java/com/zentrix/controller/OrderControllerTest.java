package com.zentrix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zentrix.model.entity.Order;
import com.zentrix.model.entity.OrderDetail;
import com.zentrix.model.entity.User;
import com.zentrix.model.request.OrderDetailRequest;
import com.zentrix.model.request.OrderRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.EmailService;
import com.zentrix.service.OrderDetailService;
import com.zentrix.service.OrderService;
import com.zentrix.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date April 09, 2025
 */
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderDetailService orderDetailService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Order order;
    private OrderDetail orderDetail;
    private User user;
    private OrderRequest orderRequest;
    private OrderDetailRequest orderDetailRequest;
    private PaginationWrapper<List<Order>> orderPagination;
    private PaginationWrapper<List<OrderDetail>> orderDetailPagination;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();

        // Initialize test data
        order = new Order();
        order.setOrderId(1L);
        order.setPaymentMethod("Credit Card");

        orderDetail = new OrderDetail();
        orderDetail.setOrdtId(1L);
        orderDetail.setQuantity(2);
        orderDetail.setUnitPrice(1000);
        orderDetail.setAmountNotVat(2000.0F);
        orderDetail.setVatRate(10.0F);

        user = new User();
        user.setUserId(1L);
        user.setEmail("user@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("1234567890");

        orderRequest = new OrderRequest();
        orderDetailRequest = new OrderDetailRequest();

        orderPagination = new PaginationWrapper.Builder<List<Order>>()
                .setData(Arrays.asList(order))
                .setPage(0)
                .setSize(10)
                .setTotalPages(1)
                .setTotalElements(1)
                .build();

        // Use PaginationWrapper.Builder to create orderDetailPagination
        orderDetailPagination = new PaginationWrapper.Builder<List<OrderDetail>>()
                .setData(Arrays.asList(orderDetail))
                .setPage(0)
                .setSize(10)
                .setTotalPages(1)
                .setTotalElements(1)
                .build();
    }

    @Test
    @WithMockUser(roles = { "SELLER STAFF", "ADMIN" })
    void testCreateOrder_Success() {
        // Arrange
        when(orderService.addOrder(any(OrderRequest.class))).thenReturn(order);

        // Act
        ResponseEntity<ResponseObject<Order>> response = orderController.createOrder(orderRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.CREATED.value(), response.getBody().getCode());
        assertEquals("Create user successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getContent());
        assertEquals("Credit Card", response.getBody().getContent().getPaymentMethod());

        // Verify
        verify(orderService, times(1)).addOrder(any(OrderRequest.class));
    }

    @Test
    @WithMockUser(roles = { "CUSTOMER", "ADMIN" })
    void testCreateOrderDetail_Success() {
        // Arrange
        when(orderDetailService.saveOrderDetail(any(OrderDetailRequest.class))).thenReturn(orderDetail);

        // Act
        ResponseEntity<ResponseObject<OrderDetail>> response = orderController.createOrderDetail(orderDetailRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.CREATED.value(), response.getBody().getCode());
        assertEquals("Create user successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getContent());
        assertEquals(2, response.getBody().getContent().getQuantity());

        // Verify
        verify(orderDetailService, times(1)).saveOrderDetail(any(OrderDetailRequest.class));
    }

    @Test
    @WithMockUser(roles = { "SELLER STAFF", "ADMIN" })
    void testGetAllOrders_Success() {
        // Arrange
        when(orderService.findAllOrders(0, 10)).thenReturn(orderPagination);

        // Act
        ResponseEntity<ResponseObject<List<Order>>> response = orderController.getAllOrders(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
        assertEquals("Get orders list successfully!", response.getBody().getMessage());
        assertNotNull(response.getBody().getContent());
        assertFalse(response.getBody().getContent().isEmpty());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Credit Card", response.getBody().getContent().get(0).getPaymentMethod());

        // Verify
        verify(orderService, times(1)).findAllOrders(0, 10);
    }

    @Test
    @WithMockUser(roles = { "SELLER STAFF", "ADMIN" })
    void testGetOrderById_Success() {
        // Arrange
        Long orderId = 1L;
        when(orderService.findOrderById(orderId)).thenReturn(order);

        // Act
        ResponseEntity<ResponseObject<Order>> response = orderController.getOrderById(orderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
        assertEquals("Get user by Id successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getContent());
        assertEquals("Credit Card", response.getBody().getContent().getPaymentMethod());

        // Verify
        verify(orderService, times(1)).findOrderById(orderId);
    }

    @Test
    @WithMockUser(roles = { "SELLER STAFF", "ADMIN" })
    void testGetOrderDetails_Success() {
        // Arrange
        Long orderId = 1L;
        when(orderService.findOrderById(orderId)).thenReturn(order);
        when(orderDetailService.findByOrderID(order, 0, 10)).thenReturn(orderDetailPagination);

        // Act
        ResponseEntity<ResponseObject<List<OrderDetail>>> response = orderController.getOrderDetails(orderId, 0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
        assertEquals("Get customers list successfully!", response.getBody().getMessage());
        assertNotNull(response.getBody().getContent());
        assertFalse(response.getBody().getContent().isEmpty());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(2, response.getBody().getContent().get(0).getQuantity());

        // Verify
        verify(orderService, times(1)).findOrderById(orderId);
        verify(orderDetailService, times(1)).findByOrderID(order, 0, 10);
    }

    @Test
    @WithMockUser(roles = { "CUSTOMER", "ADMIN" })
    void testGetOrdersByUser_Success() {
        // Arrange
        Long userId = 1L;
        when(orderService.findOrdersByUser(any(User.class), eq(0), eq(10))).thenReturn(orderPagination);

        // Act
        ResponseEntity<ResponseObject<List<Order>>> response = orderController.getOrdersByUser(userId, 0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
        assertEquals("Get user's orders successfully!", response.getBody().getMessage());
        assertNotNull(response.getBody().getContent());
        assertFalse(response.getBody().getContent().isEmpty());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Credit Card", response.getBody().getContent().get(0).getPaymentMethod());

        // Verify
        verify(orderService, times(1)).findOrdersByUser(any(User.class), eq(0), eq(10));
    }

    @Test
    @WithMockUser(roles = { "SELLER STAFF", "ADMIN" })
    void testSearchOrders_Success() {
        // Arrange
        String keyword = "test";
        when(orderService.searchOrder(keyword, 0, 10)).thenReturn(orderPagination);

        // Act
        ResponseEntity<ResponseObject<List<Order>>> response = orderController.searchOrders(keyword, 0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
        assertEquals("Get customers list successfully!", response.getBody().getMessage());
        assertNotNull(response.getBody().getContent());
        assertFalse(response.getBody().getContent().isEmpty());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Credit Card", response.getBody().getContent().get(0).getPaymentMethod());

        // Verify
        verify(orderService, times(1)).searchOrder(keyword, 0, 10);
    }

    @Test
    @WithMockUser(roles = { "SELLER STAFF", "SHIPPER", "ADMIN" })
    void testUpdateOrder_Success() {
        // Arrange
        Long orderId = 1L;
        Order updatedOrder = new Order();
        updatedOrder.setOrderId(orderId);
        updatedOrder.setPaymentMethod("Updated Payment Method");
        when(orderService.updateOrder(eq(orderId), any(OrderRequest.class))).thenReturn(updatedOrder);

        // Act
        ResponseEntity<ResponseObject<Order>> response = orderController.updateOrder(orderId, orderRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
        assertEquals("Update user successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getContent());
        assertEquals(updatedOrder, response.getBody().getContent());

        // Verify
        verify(orderService, times(1)).updateOrder(eq(orderId), any(OrderRequest.class));
    }

    @Test
    @WithMockUser(roles = { "SELLER STAFF", "ADMIN" })
    void testGenerateInvoiceAndSendEmail_Success() throws Exception {
        // Arrange
        Long orderId = 1L;
        order.setUserId(user);
        when(orderService.findOrderById(orderId)).thenReturn(order);
        when(userService.findUserByUserId(user.getUserId())).thenReturn(user);
        when(orderDetailService.findByOrderID(order, 0, Integer.MAX_VALUE)).thenReturn(orderDetailPagination);
        doNothing().when(emailService).sendEmailWithHtmlBodyAndFilePath(anyString(), any(), any(), anyString(),
                anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders/{orderId}/generateInvoiceAndSendEmail", orderId))
                .andExpect(status().isOk());

        // Verify
        verify(orderService, times(1)).findOrderById(orderId);
        verify(userService, times(1)).findUserByUserId(user.getUserId());
        verify(orderDetailService, times(1)).findByOrderID(order, 0, Integer.MAX_VALUE);
        verify(emailService, times(1)).sendEmailWithHtmlBodyAndFilePath(anyString(), any(), any(), anyString(),
                anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = { "SELLER STAFF", "ADMIN" })
    void testGenerateInvoiceAndSendEmail_OrderNotFound() throws Exception {
        // Arrange
        Long orderId = 1L;
        when(orderService.findOrderById(orderId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders/{orderId}/generateInvoiceAndSendEmail", orderId))
                .andExpect(status().isInternalServerError());

        // Verify
        verify(orderService, times(1)).findOrderById(orderId);
        verify(userService, never()).findUserByUserId(anyLong());
        verify(orderDetailService, never()).findByOrderID(any(), anyInt(), anyInt());
        verify(emailService, never()).sendEmailWithHtmlBodyAndFilePath(anyString(), any(), any(), anyString(),
                anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = { "SELLER STAFF", "ADMIN" })
    void testGenerateInvoiceAndSendEmail_UserNotFound() throws Exception {
        // Arrange
        Long orderId = 1L;
        order.setUserId(user);
        when(orderService.findOrderById(orderId)).thenReturn(order);
        when(userService.findUserByUserId(user.getUserId())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders/{orderId}/generateInvoiceAndSendEmail", orderId))
                .andExpect(status().isInternalServerError());

        // Verify
        verify(orderService, times(1)).findOrderById(orderId);
        verify(userService, times(1)).findUserByUserId(user.getUserId());
        verify(orderDetailService, never()).findByOrderID(any(), anyInt(), anyInt());
        verify(emailService, never()).sendEmailWithHtmlBodyAndFilePath(anyString(), any(), any(), anyString(),
                anyString(), anyString());
    }
}