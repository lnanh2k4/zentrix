package com.zentrix.service;

import com.zentrix.model.entity.Cart;
import com.zentrix.model.entity.CartProductTypeVariation;
import com.zentrix.model.entity.ProductTypeVariation;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.repository.CartProductTypeVariationRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartProductTypeVariationServiceImpl implements CartProductTypeVariationService {

    CartProductTypeVariationRepository repository;
    ProductTypeVariationService productTypeVariationService;

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public CartProductTypeVariation addToCart(Cart cart, ProductTypeVariation productTypeVariation, Integer quantity,
            String variCode) {
        if (cart == null || cart.getCartId() == null) {
            throw new IllegalArgumentException("Cart cannot be null");
        }
        if (productTypeVariation == null || productTypeVariation.getProdTypeVariId() == null) {
            throw new IllegalArgumentException("ProductTypeVariation cannot be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        ProductTypeVariation existingProductTypeVariation = productTypeVariationService
                .getById(productTypeVariation.getProdTypeVariId());
        if (existingProductTypeVariation == null) {
            throw new ActionFailedException(AppCode.PRODUCT_TYPE_VARIATION_NOT_FOUND);
        }

        // Kiểm tra xem cặp variCode và prodTypeVariId đã tồn tại trong giỏ hàng chưa
        CartProductTypeVariation existingItem = repository.findByCartIdAndProdTypeVariIdAndVariCode(cart,
                productTypeVariation, variCode);

        if (existingItem != null) {
            // Nếu đã tồn tại, tăng quantity lên 1 (hoặc cộng thêm quantity từ tham số nếu
            // muốn)
            existingItem.setQuantity(existingItem.getQuantity() + 1); // Hoặc + quantity nếu muốn cộng nhiều hơn
            return repository.save(existingItem);
        }

        // Nếu không tồn tại, tạo mới
        CartProductTypeVariation newItem = CartProductTypeVariation.builder()
                .cartId(cart)
                .prodTypeVariId(productTypeVariation)
                .quantity(quantity)
                .variCode(variCode)
                .build();
        return repository.save(newItem);
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public boolean removeFromCart(Long cartProductTypeVariationId) {
        if (cartProductTypeVariationId == null || cartProductTypeVariationId <= 0) {
            throw new IllegalArgumentException("Invalid CartProductTypeVariation ID: " + cartProductTypeVariationId);
        }

        if (!repository.existsById(cartProductTypeVariationId)) {
            throw new ActionFailedException(AppCode.CART_NOT_FOUND);
        }

        repository.deleteById(cartProductTypeVariationId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public CartProductTypeVariation updateQuantity(Long cartProductTypeVariationId, Integer newQuantity) {
        if (cartProductTypeVariationId == null || cartProductTypeVariationId <= 0) {
            throw new IllegalArgumentException("Invalid CartProductTypeVariation ID: " + cartProductTypeVariationId);
        }
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        CartProductTypeVariation cartItem = repository.findById(cartProductTypeVariationId)
                .orElseThrow(() -> new ActionFailedException(AppCode.CART_NOT_FOUND));

        cartItem.setQuantity(newQuantity);
        return repository.save(cartItem);
    }
}