package com.zentrix.service;

import com.zentrix.model.entity.Order;
import com.zentrix.model.entity.OrderDetail;
import com.zentrix.model.entity.ProductTypeBranch;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.OrderDetailRequest;
import com.zentrix.model.request.ProductTypeBranchRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.OrderDetailRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/*
* @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
* @date February 17, 2025
*/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderDetailServiceImpl implements OrderDetailService {

    OrderDetailRepository orderDetailRepository;
    OrderService orderService;
    ProductTypeBranchService productTypeBranchService;

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public OrderDetail saveOrderDetail(OrderDetailRequest orderDetailRequest) {

        // Tạo OrderDetail mới
        OrderDetail orderDetail = new OrderDetail();
        Order order = orderService.findOrderById(orderDetailRequest.getOrderId());
        orderDetail.setOrderId(order);

        // Lấy ProductTypeBranch từ prodTypeBranchId
        ProductTypeBranch productTypeBranch = productTypeBranchService
                .findProductTypeBranchById(orderDetailRequest.getProdTypeBranchId());
        if (productTypeBranch == null) {
            throw new ActionFailedException(AppCode.PRODUCT_TYPE_BRANCH_NOT_FOUND);
        }

        // Kiểm tra số lượng trong ProductTypeBranch
        int availableQuantity = productTypeBranch.getQuantity();
        int requestedQuantity = orderDetailRequest.getQuantity();

        // Trừ số lượng trong ProductTypeBranch
        productTypeBranch.setQuantity(availableQuantity - requestedQuantity);
        productTypeBranchService.updateProductTypeBranch(productTypeBranch.getProdTypeBrchId(),
                ProductTypeBranchRequest.builder()
                        .prodTypeId(productTypeBranch.getProdTypeId().getProdTypeId())
                        .brchId(productTypeBranch.getBrchId().getBrchId())
                        .quantity(productTypeBranch.getQuantity())
                        .build());

        // Thiết lập các thuộc tính cho OrderDetail
        orderDetail.setProdTypeBranchId(productTypeBranch);
        orderDetail.setQuantity(orderDetailRequest.getQuantity());
        orderDetail.setUnitPrice(orderDetailRequest.getUnitPrice());
        orderDetail.setAmountNotVat(orderDetailRequest.getAmountNotVat());
        orderDetail.setVatRate(orderDetailRequest.getVatRate());
        orderDetail.setVariation(orderDetailRequest.getVariation());

        // Lưu OrderDetail vào cơ sở dữ liệu
        return orderDetailRepository.save(orderDetail);

    }

    @Override
    public OrderDetail findOrderDetail(long ordtId) {
        return orderDetailRepository.findById(ordtId)
                .orElseThrow(() -> new ActionFailedException(AppCode.ORDER_DETAIL_NOT_FOUND));
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void updateOrderDetail(OrderDetail orderDetail) {
        if (!orderDetailRepository.existsById(orderDetail.getOrdtId())) {
            throw new ActionFailedException(AppCode.ORDER_DETAIL_NOT_FOUND);
        }
        try {
            orderDetailRepository.save(orderDetail);
        } catch (Exception e) {
            throw new ActionFailedException(AppCode.ORDER_DETAIL_UPDATE_FAILED);
        }
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void deleteOrderDetail(long ordtId) {
        if (!orderDetailRepository.existsById(ordtId)) {
            throw new ActionFailedException(AppCode.ORDER_DETAIL_DELETE_FAILED);
        }
        try {
            orderDetailRepository.deleteById(ordtId);
        } catch (Exception e) {
            throw new ActionFailedException(AppCode.ORDER_DETAIL_DELETE_FAILED);

        }
    }

    @Override
    public PaginationWrapper<List<OrderDetail>> findAllOrderDetail(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDetail> orderDetailPage = orderDetailRepository.findAll(pageable);
            return new PaginationWrapper.Builder<List<OrderDetail>>()
                    .setData(orderDetailPage.getContent())
                    .setPaginationInfo(
                            orderDetailPage)
                    .build();
        } catch (Exception e) {
            throw new ActionFailedException(AppCode.ORDER_DETAIL_NOT_FOUND);

        }
    }

    @Override
    public PaginationWrapper<List<OrderDetail>> findByOrderID(Order orderId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDetail> orderDetailPage = orderDetailRepository.findByOrderId(orderId, pageable);
            return new PaginationWrapper.Builder<List<OrderDetail>>()
                    .setData(orderDetailPage.getContent())
                    .setPaginationInfo(
                            orderDetailPage)
                    .build();

        } catch (Exception e) {
            throw new ActionFailedException(AppCode.ORDER_DETAIL_NOT_FOUND);

        }
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public void restoreQuantitiesForOrder(Long orderId) {
        Order order = orderService.findOrderById(orderId);
        List<OrderDetail> orderDetails = findByOrderID(order, 0, Integer.MAX_VALUE).getData();

        for (OrderDetail orderDetail : orderDetails) {
            ProductTypeBranch productTypeBranch = orderDetail.getProdTypeBranchId();
            if (productTypeBranch != null) {
                int currentQuantity = productTypeBranch.getQuantity();
                int quantityToRestore = orderDetail.getQuantity();
                productTypeBranch.setQuantity(currentQuantity + quantityToRestore);

                productTypeBranchService.updateProductTypeBranch(
                        productTypeBranch.getProdTypeBrchId(),
                        ProductTypeBranchRequest.builder()
                                .prodTypeId(productTypeBranch.getProdTypeId().getProdTypeId())
                                .brchId(productTypeBranch.getBrchId().getBrchId())
                                .quantity(productTypeBranch.getQuantity())
                                .build());
            }
        }
    }
}
