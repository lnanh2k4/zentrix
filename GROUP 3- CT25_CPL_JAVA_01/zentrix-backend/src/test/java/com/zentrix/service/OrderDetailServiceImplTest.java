package com.zentrix.service;

import com.zentrix.model.entity.Branch;
import com.zentrix.model.entity.Order;
import com.zentrix.model.entity.OrderDetail;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.ProductTypeBranch;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.OrderDetailRequest;
import com.zentrix.model.request.ProductTypeBranchRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.OrderDetailRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date April 08, 2025
 */
@ExtendWith(MockitoExtension.class)
public class OrderDetailServiceImplTest {

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private ProductTypeBranchService productTypeBranchService;

    @InjectMocks
    private OrderDetailServiceImpl orderDetailService;

    private Order order;
    private ProductTypeBranch productTypeBranch;
    private OrderDetail orderDetail;
    private OrderDetailRequest orderDetailRequest;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setOrderId(1L);

        productTypeBranch = new ProductTypeBranch();
        productTypeBranch.setProdTypeBrchId(1L);
        productTypeBranch.setQuantity(10);

        orderDetail = new OrderDetail();
        orderDetail.setOrdtId(1L);
        orderDetail.setOrderId(order);
        orderDetail.setProdTypeBranchId(productTypeBranch);
        orderDetail.setQuantity(2);
        orderDetail.setUnitPrice(100);
        orderDetail.setAmountNotVat(200.0F);
        orderDetail.setVatRate(10.0F);

        orderDetailRequest = new OrderDetailRequest();
        orderDetailRequest.setOrderId(1L);
        orderDetailRequest.setProdTypeBranchId(1L);
        orderDetailRequest.setQuantity(2);
        orderDetailRequest.setUnitPrice(10);
        orderDetailRequest.setAmountNotVat(200.0F);
        orderDetailRequest.setVatRate(10.0F);
    }

    @Test
    void testDeleteOrderDetail_Success() {
        Long ordtId = 1L;

        when(orderDetailRepository.existsById(ordtId)).thenReturn(true);
        doNothing().when(orderDetailRepository).deleteById(ordtId);

        orderDetailService.deleteOrderDetail(ordtId);

        verify(orderDetailRepository, times(1)).existsById(ordtId);
        verify(orderDetailRepository, times(1)).deleteById(ordtId);
    }

    @Test
    void testDeleteOrderDetail_NotFound() {
        Long ordtId = 1L;

        when(orderDetailRepository.existsById(ordtId)).thenReturn(false);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> orderDetailService.deleteOrderDetail(ordtId));
        assertEquals(AppCode.ORDER_DETAIL_DELETE_FAILED.getCode(), exception.getErrors().getCode());
        verify(orderDetailRepository, times(1)).existsById(ordtId);
        verify(orderDetailRepository, never()).deleteById(ordtId);
    }

    @Test
    void testDeleteOrderDetail_Failure() {
        Long ordtId = 1L;

        when(orderDetailRepository.existsById(ordtId)).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(orderDetailRepository).deleteById(ordtId);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> orderDetailService.deleteOrderDetail(ordtId));
        assertEquals(AppCode.ORDER_DETAIL_DELETE_FAILED.getCode(), exception.getErrors().getCode());
        verify(orderDetailRepository, times(1)).existsById(ordtId);
        verify(orderDetailRepository, times(1)).deleteById(ordtId);
    }

    @Test
    void testFindAllOrderDetail_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderDetail> page = new PageImpl<>(List.of(orderDetail), pageable, 1);
        when(orderDetailRepository.findAll(pageable)).thenReturn(page);

        PaginationWrapper<List<OrderDetail>> result = orderDetailService.findAllOrderDetail(0, 10);

        assertNotNull(result);
        assertFalse(result.getData().isEmpty());
        assertEquals(1, result.getData().size());
        assertEquals(orderDetail, result.getData().get(0));
        verify(orderDetailRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindAllOrderDetail_Failure() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderDetailRepository.findAll(pageable)).thenThrow(new RuntimeException("Database error"));

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> orderDetailService.findAllOrderDetail(0, 10));
        assertEquals(AppCode.ORDER_DETAIL_NOT_FOUND.getCode(), exception.getErrors().getCode());
        verify(orderDetailRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindByOrderID_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderDetail> page = new PageImpl<>(List.of(orderDetail), pageable, 1);
        when(orderDetailRepository.findByOrderId(order, pageable)).thenReturn(page);

        PaginationWrapper<List<OrderDetail>> result = orderDetailService.findByOrderID(order, 0, 10);

        assertNotNull(result);
        assertFalse(result.getData().isEmpty());
        assertEquals(1, result.getData().size());
        assertEquals(orderDetail, result.getData().get(0));
        verify(orderDetailRepository, times(1)).findByOrderId(order, pageable);
    }

    @Test
    void testFindByOrderID_Failure() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderDetailRepository.findByOrderId(order, pageable)).thenThrow(new RuntimeException("Database error"));

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> orderDetailService.findByOrderID(order, 0, 10));
        assertEquals(AppCode.ORDER_DETAIL_NOT_FOUND.getCode(), exception.getErrors().getCode());
        verify(orderDetailRepository, times(1)).findByOrderId(order, pageable);
    }

    @Test
    void testFindOrderDetail_Success() {
        Long ordtId = 1L;
        when(orderDetailRepository.findById(ordtId)).thenReturn(Optional.of(orderDetail));

        OrderDetail result = orderDetailService.findOrderDetail(ordtId);

        assertNotNull(result);
        assertEquals(ordtId, result.getOrdtId());
        verify(orderDetailRepository, times(1)).findById(ordtId);
    }

    @Test
    void testFindOrderDetail_NotFound() {
        Long ordtId = 1L;
        when(orderDetailRepository.findById(ordtId)).thenReturn(Optional.empty());

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> orderDetailService.findOrderDetail(ordtId));
        assertEquals(AppCode.ORDER_DETAIL_NOT_FOUND.getCode(), exception.getErrors().getCode());
        verify(orderDetailRepository, times(1)).findById(ordtId);
    }

    @Test
    void testSaveOrderDetail_Success() {
        // Arrange
        // Mock OrderDetailRequest
        OrderDetailRequest orderDetailRequest = mock(OrderDetailRequest.class);
        when(orderDetailRequest.getOrderId()).thenReturn(1L);
        when(orderDetailRequest.getProdTypeBranchId()).thenReturn(2L);
        when(orderDetailRequest.getQuantity()).thenReturn(5);
        when(orderDetailRequest.getUnitPrice()).thenReturn(1000);
        when(orderDetailRequest.getAmountNotVat()).thenReturn(5000.0F);
        when(orderDetailRequest.getVatRate()).thenReturn(10.0F);

        // Mock Order
        Order order = mock(Order.class);
        when(orderService.findOrderById(1L)).thenReturn(order);

        // Mock ProductTypeBranch and its dependencies
        ProductTypeBranch productTypeBranch = mock(ProductTypeBranch.class);
        when(productTypeBranchService.findProductTypeBranchById(2L)).thenReturn(productTypeBranch);

        // Mock ProductType and Branch
        ProductType productType = mock(ProductType.class);
        when(productType.getProdTypeId()).thenReturn(3L);
        Branch branch = mock(Branch.class);
        when(branch.getBrchId()).thenReturn(4L);

        // Mock ProductTypeBranch methods
        when(productTypeBranch.getProdTypeBrchId()).thenReturn(2L);
        when(productTypeBranch.getProdTypeId()).thenReturn(productType);
        when(productTypeBranch.getBrchId()).thenReturn(branch);
        when(productTypeBranch.getQuantity()).thenReturn(10); // Initial quantity

        // Mock the updateProductTypeBranch call
        ProductTypeBranch updatedProductTypeBranch = mock(ProductTypeBranch.class);
        when(productTypeBranchService.updateProductTypeBranch(eq(2L), any(ProductTypeBranchRequest.class)))
                .thenReturn(updatedProductTypeBranch);

        // Mock the saved OrderDetail
        OrderDetail savedOrderDetail = mock(OrderDetail.class);
        when(orderDetailRepository.save(any(OrderDetail.class))).thenReturn(savedOrderDetail);

        // Act
        OrderDetail result = orderDetailService.saveOrderDetail(orderDetailRequest);

        // Assert
        assertNotNull(result);
        assertEquals(savedOrderDetail, result);

        // Verify interactions
        verify(orderService, times(1)).findOrderById(1L);
        verify(productTypeBranchService, times(1)).findProductTypeBranchById(2L);
        verify(productTypeBranchService, times(1)).updateProductTypeBranch(eq(2L), any(ProductTypeBranchRequest.class));
        verify(orderDetailRepository, times(1)).save(any(OrderDetail.class));

        // Verify the OrderDetail passed to save
        verify(orderDetailRepository).save(argThat(orderDetail -> orderDetail.getOrderId() == order &&
                orderDetail.getProdTypeBranchId() == productTypeBranch &&
                orderDetail.getQuantity() == 5 &&
                orderDetail.getUnitPrice() == 1000.0 &&
                orderDetail.getAmountNotVat() == 5000.0 &&
                orderDetail.getVatRate() == 10.0));
    }

    @Test
    void testSaveOrderDetail_ProductTypeBranchNotFound() {
        when(orderService.findOrderById(orderDetailRequest.getOrderId())).thenReturn(order);
        when(productTypeBranchService.findProductTypeBranchById(orderDetailRequest.getProdTypeBranchId()))
                .thenReturn(null);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> orderDetailService.saveOrderDetail(orderDetailRequest));
        assertEquals(AppCode.PRODUCT_TYPE_BRANCH_NOT_FOUND.getCode(), exception.getErrors().getCode());
        verify(orderService, times(1)).findOrderById(orderDetailRequest.getOrderId());
        verify(productTypeBranchService, times(1)).findProductTypeBranchById(orderDetailRequest.getProdTypeBranchId());
        verify(productTypeBranchService, never()).updateProductTypeBranch(anyLong(),
                any(ProductTypeBranchRequest.class));
    }

    @Test
    void testUpdateOrderDetail_Success() {
        when(orderDetailRepository.existsById(orderDetail.getOrdtId())).thenReturn(true);
        when(orderDetailRepository.save(orderDetail)).thenReturn(orderDetail);

        orderDetailService.updateOrderDetail(orderDetail);

        verify(orderDetailRepository, times(1)).existsById(orderDetail.getOrdtId());
        verify(orderDetailRepository, times(1)).save(orderDetail);
    }

    @Test
    void testUpdateOrderDetail_NotFound() {
        when(orderDetailRepository.existsById(orderDetail.getOrdtId())).thenReturn(false);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> orderDetailService.updateOrderDetail(orderDetail));
        assertEquals(AppCode.ORDER_DETAIL_NOT_FOUND.getCode(), exception.getErrors().getCode());
        verify(orderDetailRepository, times(1)).existsById(orderDetail.getOrdtId());
        verify(orderDetailRepository, never()).save(any(OrderDetail.class));
    }

    @Test
    void testUpdateOrderDetail_Failure() {
        when(orderDetailRepository.existsById(orderDetail.getOrdtId())).thenReturn(true);
        when(orderDetailRepository.save(orderDetail)).thenThrow(new RuntimeException("Database error"));

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> orderDetailService.updateOrderDetail(orderDetail));
        assertEquals(AppCode.ORDER_DETAIL_UPDATE_FAILED.getCode(), exception.getErrors().getCode());
        verify(orderDetailRepository, times(1)).existsById(orderDetail.getOrdtId());
        verify(orderDetailRepository, times(1)).save(orderDetail);
    }
}