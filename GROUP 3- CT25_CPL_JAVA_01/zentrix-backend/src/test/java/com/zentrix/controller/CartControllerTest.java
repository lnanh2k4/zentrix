package com.zentrix.controller;

import com.zentrix.model.entity.Cart;
import com.zentrix.model.entity.CartProductTypeVariation;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.User;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.CartProductTypeVariationService;
import com.zentrix.service.CartService;
import com.zentrix.service.EmailService;
import com.zentrix.service.ProductTypeService;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date April 08, 2025
 */
@ExtendWith(MockitoExtension.class)
public class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private CartProductTypeVariationService cartProductTypeVariationService;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private ProductTypeService productTypeService;

    @InjectMocks
    private CartController cartController;

    private User user;
    private Cart cart;
    private CartProductTypeVariation cartProductTypeVariation;
    private ProductType productType;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);

        cart = new Cart();
        cart.setCartId(1L);
        cart.setUserId(user);

        cartProductTypeVariation = new CartProductTypeVariation();
        cartProductTypeVariation.setCartProductTypeVariationId(1L);
        cartProductTypeVariation.setCartId(cart);
        cartProductTypeVariation.setQuantity(2);

        productType = new ProductType();
        productType.setProdTypeId(1L);
        productType.setProdTypeName("Laptop");
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testAddToCart_Success() {
        Long userId = 1L;
        Long cartId = 1L;
        Long productTypeVariId = 1L;
        Integer quantity = 2;
        String varicode = "abc";
        when(userService.findUserByUserId(userId)).thenReturn(user);
        when(cartService.addProductToCart(cartId, productTypeVariId, quantity, user, varicode)).thenReturn(cart);

        ResponseEntity<ResponseObject<Cart>> response = cartController.addToCart(userId, cartId, productTypeVariId,
                quantity, varicode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(cart, response.getBody().getContent());
        assertEquals("Product added to cart successfully!", response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartService, times(1)).addProductToCart(cartId, productTypeVariId, quantity, user, varicode);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testAddToCart_UserNotFound() {
        Long userId = 1L;
        Long cartId = 1L;
        Long productTypeVariId = 1L;
        Integer quantity = 2;
        String varicode = "abc";
        when(userService.findUserByUserId(userId)).thenReturn(null);

        ResponseEntity<ResponseObject<Cart>> response = cartController.addToCart(userId, cartId, productTypeVariId,
                quantity, varicode);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getContent());
        assertEquals("User not found with ID: " + userId, response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);

    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testAddToCart_ServiceThrowsException() {
        Long userId = 1L;
        Long cartId = 1L;
        Long productTypeVariId = 1L;
        Integer quantity = 2;
        String varicode = "abbc";
        when(userService.findUserByUserId(userId)).thenReturn(user);
        when(cartService.addProductToCart(cartId, productTypeVariId, quantity, user,
                varicode))
                .thenThrow(new RuntimeException("Failed to add product"));

        ResponseEntity<ResponseObject<Cart>> response = cartController.addToCart(userId, cartId, productTypeVariId,
                quantity, varicode);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getContent());
        assertEquals("Failed to add product to cart: Failed to add product", response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartService, times(1)).addProductToCart(cartId, productTypeVariId, quantity, user, varicode);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateCart_Success() {
        Long userId = 1L;

        when(userService.findUserByUserId(userId)).thenReturn(user);
        when(cartService.createCart(user)).thenReturn(cart);

        ResponseEntity<ResponseObject<Cart>> response = cartController.createCart(userId);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(cart, response.getBody().getContent());
        assertEquals("Cart created successfully!", response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartService, times(1)).createCart(user);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateCart_UserNotFound() {
        Long userId = 1L;

        when(userService.findUserByUserId(userId)).thenReturn(null);

        ResponseEntity<ResponseObject<Cart>> response = cartController.createCart(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getContent());
        assertEquals("User not found with ID: " + userId, response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartService, never()).createCart(any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateCart_ServiceThrowsException() {
        Long userId = 1L;

        when(userService.findUserByUserId(userId)).thenReturn(user);
        when(cartService.createCart(user)).thenThrow(new RuntimeException("Failed to create cart"));

        ResponseEntity<ResponseObject<Cart>> response = cartController.createCart(userId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getContent());
        assertEquals("Failed to create cart: Failed to create cart", response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartService, times(1)).createCart(user);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetCartItems_Success() {
        Long userId = 1L;
        Long cartId = 1L;
        List<CartProductTypeVariation> items = Arrays.asList(cartProductTypeVariation);

        when(userService.findUserByUserId(userId)).thenReturn(user);
        when(cartService.getCartItems(cartId, user)).thenReturn(items);

        ResponseEntity<ResponseObject<List<CartProductTypeVariation>>> response = cartController.getCartItems(userId,
                cartId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(items, response.getBody().getContent());
        assertEquals("Retrieved cart items successfully!", response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartService, times(1)).getCartItems(cartId, user);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetCartItems_UserNotFound() {
        Long userId = 1L;
        Long cartId = 1L;

        when(userService.findUserByUserId(userId)).thenReturn(null);

        ResponseEntity<ResponseObject<List<CartProductTypeVariation>>> response = cartController.getCartItems(userId,
                cartId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getContent());
        assertEquals("User not found with ID: " + userId, response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartService, never()).getCartItems(anyLong(), any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetCartItems_ServiceThrowsException() {
        Long userId = 1L;
        Long cartId = 1L;

        when(userService.findUserByUserId(userId)).thenReturn(user);
        when(cartService.getCartItems(cartId, user)).thenThrow(new RuntimeException("Failed to retrieve items"));

        ResponseEntity<ResponseObject<List<CartProductTypeVariation>>> response = cartController.getCartItems(userId,
                cartId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getContent());
        assertEquals("Failed to retrieve cart items: Failed to retrieve items", response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartService, times(1)).getCartItems(cartId, user);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetUserCart_Success() {
        Long userId = 1L;
        int page = 0;
        int size = 10;
        List<Cart> carts = Arrays.asList(cart);
        PaginationWrapper<List<Cart>> wrapper = new PaginationWrapper.Builder<List<Cart>>()
                .setData(carts)
                .setPage(page)
                .setSize(size)
                .setTotalPages(1)
                .setTotalElements(carts.size())
                .build();

        when(userService.findUserByUserId(userId)).thenReturn(user);
        when(cartService.getCartByUserId(user, page, size)).thenReturn(wrapper);

        ResponseEntity<ResponseObject<List<Cart>>> response = cartController.getUserCart(userId, page, size);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(carts, response.getBody().getContent());
        assertEquals("Retrieved user's cart successfully!", response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartService, times(1)).getCartByUserId(user, page, size);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetUserCart_UserNotFound() {
        Long userId = 1L;
        int page = 0;
        int size = 10;

        when(userService.findUserByUserId(userId)).thenReturn(null);

        ResponseEntity<ResponseObject<List<Cart>>> response = cartController.getUserCart(userId, page, size);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getContent());
        assertEquals("User not found with ID: " + userId, response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartService, never()).getCartByUserId(any(), anyInt(), anyInt());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testRemoveFromCart_Success() {
        Long userId = 1L;
        Long cartProductTypeVariationId = 1L;

        when(userService.findUserByUserId(userId)).thenReturn(user);
        when(cartProductTypeVariationService.removeFromCart(cartProductTypeVariationId)).thenReturn(true);

        ResponseEntity<ResponseObject<Void>> response = cartController.removeFromCart(userId,
                cartProductTypeVariationId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Removed product from cart successfully!", response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartProductTypeVariationService, times(1)).removeFromCart(cartProductTypeVariationId);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testRemoveFromCart_UserNotFound() {
        Long userId = 1L;
        Long cartProductTypeVariationId = 1L;

        when(userService.findUserByUserId(userId)).thenReturn(null);

        ResponseEntity<ResponseObject<Void>> response = cartController.removeFromCart(userId,
                cartProductTypeVariationId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not found with ID: " + userId, response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartProductTypeVariationService, never()).removeFromCart(anyLong());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateCartQuantity_Success() {
        Long userId = 1L;
        Long cartProductTypeVariationId = 1L;
        Integer quantity = 3;

        when(userService.findUserByUserId(userId)).thenReturn(user);
        when(cartProductTypeVariationService.updateQuantity(cartProductTypeVariationId, quantity))
                .thenReturn(cartProductTypeVariation);

        ResponseEntity<ResponseObject<CartProductTypeVariation>> response = cartController.updateCartQuantity(userId,
                cartProductTypeVariationId, quantity);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(cartProductTypeVariation, response.getBody().getContent());
        assertEquals("Updated product quantity in cart successfully!", response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartProductTypeVariationService, times(1)).updateQuantity(cartProductTypeVariationId, quantity);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateCartQuantity_UserNotFound() {
        Long userId = 1L;
        Long cartProductTypeVariationId = 1L;
        Integer quantity = 3;

        when(userService.findUserByUserId(userId)).thenReturn(null);

        ResponseEntity<ResponseObject<CartProductTypeVariation>> response = cartController.updateCartQuantity(userId,
                cartProductTypeVariationId, quantity);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getContent());
        assertEquals("User not found with ID: " + userId, response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartProductTypeVariationService, never()).updateQuantity(anyLong(), anyInt());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateCartQuantity_ServiceThrowsException() {
        Long userId = 1L;
        Long cartProductTypeVariationId = 1L;
        Integer quantity = 3;

        when(userService.findUserByUserId(userId)).thenReturn(user);
        when(cartProductTypeVariationService.updateQuantity(cartProductTypeVariationId, quantity))
                .thenThrow(new RuntimeException("Failed to update quantity"));

        ResponseEntity<ResponseObject<CartProductTypeVariation>> response = cartController.updateCartQuantity(userId,
                cartProductTypeVariationId, quantity);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getContent());
        assertEquals("Failed to update product quantity: Failed to update quantity", response.getBody().getMessage());
        verify(userService, times(1)).findUserByUserId(userId);
        verify(cartProductTypeVariationService, times(1)).updateQuantity(cartProductTypeVariationId, quantity);
    }
}