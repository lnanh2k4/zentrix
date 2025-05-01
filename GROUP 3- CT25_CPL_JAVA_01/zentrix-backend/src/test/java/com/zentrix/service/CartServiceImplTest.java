package com.zentrix.service;

import com.zentrix.model.entity.Cart;
import com.zentrix.model.entity.CartProductTypeVariation;
import com.zentrix.model.entity.ProductTypeVariation;
import com.zentrix.model.entity.User;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.CartRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date April 08, 2025
 */
@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartProductTypeVariationService cartProductTypeVariationService;

    @Mock
    private ProductTypeVariationService productTypeVariationService;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private User user;
    private ProductTypeVariation productTypeVariation;
    private CartProductTypeVariation cartProductTypeVariation;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        String varicode = "abc";

        cart = new Cart();
        cart.setCartId(1L);
        cart.setUserId(user);
        cart.setCartProductTypeVariations(new ArrayList<>());

        productTypeVariation = new ProductTypeVariation();
        productTypeVariation.setProdTypeVariId(1L);

        cartProductTypeVariation = new CartProductTypeVariation();
        cartProductTypeVariation.setCartId(cart);
        cartProductTypeVariation.setProdTypeVariId(productTypeVariation);
        cartProductTypeVariation.setQuantity(2);
    }

    @Test
    void testAddProductToCart_Success() {
        Long cartId = 1L;
        Long productTypeVariationId = 1L;
        Integer quantity = 2;
        String varicode = "abc";

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productTypeVariationService.getById(productTypeVariationId)).thenReturn(productTypeVariation);
        when(cartProductTypeVariationService.addToCart(cart, productTypeVariation, quantity,
                varicode))
                .thenReturn(cartProductTypeVariation);

        Cart result = cartService.addProductToCart(cartId, productTypeVariationId, quantity, user, varicode);

        assertNotNull(result);
        assertEquals(cartId, result.getCartId());
        verify(cartRepository, times(1)).findById(cartId);
        verify(productTypeVariationService, times(1)).getById(productTypeVariationId);
        verify(cartProductTypeVariationService, times(1)).addToCart(cart, productTypeVariation, quantity, varicode);
    }

    @Test
    void testAddProductToCart_UserNotMatch() {
        Long cartId = 1L;
        Long productTypeVariationId = 1L;
        Integer quantity = 2;
        String varicode = "abc";

        User differentUser = new User();
        differentUser.setUserId(2L);
        cart.setUserId(differentUser);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> cartService.addProductToCart(cartId, productTypeVariationId, quantity, user, varicode));
        assertEquals(AppCode.USER_EXISTED.getCode(), exception.getErrors().getCode());
        verify(cartRepository, times(1)).findById(cartId);
    }

    @Test
    void testAddProductToCart_ProductTypeVariationNotFound() {
        Long cartId = 1L;
        Long productTypeVariationId = 1L;
        Integer quantity = 2;
        String varicode = "abc";

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productTypeVariationService.getById(productTypeVariationId)).thenReturn(null);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> cartService.addProductToCart(cartId, productTypeVariationId, quantity, user, varicode));
        assertEquals(AppCode.PRODUCT_TYPE_VARIATION_NOT_FOUND.getCode(), exception.getErrors().getCode());
        verify(cartRepository, times(1)).findById(cartId);
        verify(productTypeVariationService, times(1)).getById(productTypeVariationId);
    }

    @Test
    void testAddProductToCart_NullUser() {
        Long cartId = 1L;
        Long productTypeVariationId = 1L;
        Integer quantity = 2;
        String varicode = "abc";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.addProductToCart(cartId, productTypeVariationId, quantity, null, varicode));
        assertEquals("User cannot be null", exception.getMessage());
    }

    @Test
    void testCreateCart_Success() {
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.createCart(user);

        assertNotNull(result);
        assertEquals(user, result.getUserId());
        assertNotNull(result.getCartProductTypeVariations());
        assertTrue(result.getCartProductTypeVariations().isEmpty());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testCreateCart_NullUser() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.createCart(null));
        assertEquals("User cannot be null when creating a cart", exception.getMessage());
    }

    @Test
    void testGetCartById_Success() {
        Long cartId = 1L;
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        Cart result = cartService.getCartById(cartId);

        assertNotNull(result);
        assertEquals(cartId, result.getCartId());
        verify(cartRepository, times(1)).findById(cartId);
    }

    @Test
    void testGetCartById_NotFound() {
        Long cartId = 1L;
        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> cartService.getCartById(cartId));
        assertEquals(AppCode.CART_NOT_FOUND.getCode(), exception.getErrors().getCode());
        verify(cartRepository, times(1)).findById(cartId);
    }

    @Test
    void testGetCartById_InvalidId() {
        Long invalidId = 0L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.getCartById(invalidId));
        assertEquals("Invalid cart ID: " + invalidId, exception.getMessage());
    }

    @Test
    void testGetCartByUserId_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cart> page = new PageImpl<>(List.of(cart), pageable, 1);
        when(cartRepository.findByUserId(user, pageable)).thenReturn(page);

        PaginationWrapper<List<Cart>> result = cartService.getCartByUserId(user, 0, 10);

        assertNotNull(result);
        assertFalse(result.getData().isEmpty());
        assertEquals(1, result.getData().size());
        assertEquals(cart, result.getData().get(0));
        verify(cartRepository, times(1)).findByUserId(user, pageable);
    }

    @Test
    void testGetCartByUserId_NullUser() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.getCartByUserId(null, 0, 10));
        assertEquals("User cannot be null", exception.getMessage());
    }

    @Test
    void testGetCartByUserId_Failure() {
        Pageable pageable = PageRequest.of(0, 10);
        when(cartRepository.findByUserId(user, pageable)).thenThrow(new RuntimeException("Database error"));

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> cartService.getCartByUserId(user, 0, 10));
        assertEquals(AppCode.CART_NOT_FOUND.getCode(), exception.getErrors().getCode());
        verify(cartRepository, times(1)).findByUserId(user, pageable);
    }

    @Test
    void testGetCartItems_Success() {
        Long cartId = 1L;
        cart.getCartProductTypeVariations().add(cartProductTypeVariation);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        List<CartProductTypeVariation> result = cartService.getCartItems(cartId, user);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(cartProductTypeVariation, result.get(0));
        verify(cartRepository, times(1)).findById(cartId);
    }

    @Test
    void testGetCartItems_UserNotMatch() {
        Long cartId = 1L;
        User differentUser = new User();
        differentUser.setUserId(2L);
        cart.setUserId(differentUser);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> cartService.getCartItems(cartId, user));
        assertEquals(AppCode.USER_EXISTED.getCode(), exception.getErrors().getCode());
        verify(cartRepository, times(1)).findById(cartId);
    }

    @Test
    void testGetCartItems_NullUser() {
        Long cartId = 1L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.getCartItems(cartId, null));
        assertEquals("User cannot be null", exception.getMessage());
    }
}