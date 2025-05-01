package com.zentrix.service;

import com.zentrix.model.entity.Cart;
import com.zentrix.model.entity.CartProductTypeVariation;
import com.zentrix.model.entity.ProductTypeVariation;
import com.zentrix.model.entity.User;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.CartRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {

    CartRepository cartRepository;
    CartProductTypeVariationService cartProductTypeVariationService;
    ProductTypeVariationService productTypeVariationService;
    ProductTypeBranchService productTypeBranchService;

    @Override
    public PaginationWrapper<List<Cart>> getCartByUserId(User user, int page, int size) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Cart> cartPage = cartRepository.findByUserId(user, pageable);
            return new PaginationWrapper.Builder<List<Cart>>()
                    .setData(cartPage.getContent())
                    .setPaginationInfo(cartPage)
                    .build();
        } catch (Exception e) {
            throw new ActionFailedException(AppCode.CART_NOT_FOUND);
        }
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public Cart createCart(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null when creating a cart");
        }

        Cart cart = Cart.builder()
                .userId(user)
                .cartProductTypeVariations(new ArrayList<>())
                .build();
        return cartRepository.save(cart);
    }

    @Override
    public Cart getCartById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid cart ID: " + id);
        }

        return cartRepository.findById(id)
                .orElseThrow(() -> new ActionFailedException(AppCode.CART_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public Cart addProductToCart(Long cartId, Long productTypeVariationId, Integer quantity, User user,
            String variCode) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Cart cart = getCartById(cartId);

        if (!cart.getUserId().getUserId().equals(user.getUserId())) {
            throw new ActionFailedException(AppCode.USER_EXISTED);
        }

        ProductTypeVariation productTypeVariation = productTypeVariationService.getById(productTypeVariationId);
        if (productTypeVariation == null) {
            throw new ActionFailedException(AppCode.PRODUCT_TYPE_VARIATION_NOT_FOUND);
        }

        cartProductTypeVariationService.addToCart(cart, productTypeVariation, quantity, variCode);
        return cart;
    }

    @Override
    public List<CartProductTypeVariation> getCartItems(Long cartId, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Cart cart = getCartById(cartId);

        if (!cart.getUserId().getUserId().equals(user.getUserId())) {
            throw new ActionFailedException(AppCode.USER_EXISTED);
        }

        return cart.getCartProductTypeVariations();
    }

    @Override
    @Transactional(rollbackFor = { Exception.class }, isolation = Isolation.REPEATABLE_READ)
    public void clearCart(Long cartId, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Cart cart = getCartById(cartId);

        if (!cart.getUserId().getUserId().equals(user.getUserId())) {
            throw new ActionFailedException(AppCode.USER_EXISTED);
        }

        cart.getCartProductTypeVariations().clear();
        cartRepository.save(cart);
    }

}