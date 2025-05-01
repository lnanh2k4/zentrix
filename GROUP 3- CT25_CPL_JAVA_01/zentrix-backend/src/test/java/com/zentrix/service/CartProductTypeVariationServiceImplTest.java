package com.zentrix.service;

import com.zentrix.model.entity.Cart;
import com.zentrix.model.entity.CartProductTypeVariation;
import com.zentrix.model.entity.ProductTypeVariation;
import com.zentrix.model.entity.User;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.repository.CartProductTypeVariationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date April 08, 2025
 */
@ExtendWith(MockitoExtension.class)
public class CartProductTypeVariationServiceImplTest {

    @Mock
    private CartProductTypeVariationRepository repository;

    @Mock
    private ProductTypeVariationService productTypeVariationService;

    @InjectMocks
    private CartProductTypeVariationServiceImpl cartProductTypeVariationService;

    private Cart cart;
    private User user;
    private ProductTypeVariation productTypeVariation;
    private CartProductTypeVariation cartProductTypeVariation;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);

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
    void testAddToCart_Success_NewItem() {
        Integer quantity = 2;
        String varicode = "abc";
        when(productTypeVariationService.getById(productTypeVariation.getProdTypeVariId()))
                .thenReturn(productTypeVariation);
        when(repository.findByCartIdAndProdTypeVariId(cart, productTypeVariation)).thenReturn(Optional.empty());
        when(repository.save(any(CartProductTypeVariation.class))).thenReturn(cartProductTypeVariation);

        CartProductTypeVariation result = cartProductTypeVariationService.addToCart(cart, productTypeVariation,
                quantity, varicode);

        assertNotNull(result);
        assertEquals(cart, result.getCartId());
        assertEquals(productTypeVariation, result.getProdTypeVariId());
        assertEquals(quantity, result.getQuantity());
        verify(productTypeVariationService, times(1)).getById(productTypeVariation.getProdTypeVariId());
        verify(repository, times(1)).findByCartIdAndProdTypeVariId(cart, productTypeVariation);
        verify(repository, times(1)).save(any(CartProductTypeVariation.class));
    }

    @Test
    void testAddToCart_Success_UpdateExistingItem() {
        Integer quantity = 2;
        Integer existingQuantity = 3;
        cartProductTypeVariation.setQuantity(existingQuantity);
        String varicode = "abc";
        when(productTypeVariationService.getById(productTypeVariation.getProdTypeVariId()))
                .thenReturn(productTypeVariation);
        when(repository.findByCartIdAndProdTypeVariId(cart, productTypeVariation))
                .thenReturn(Optional.of(cartProductTypeVariation));
        when(repository.save(cartProductTypeVariation)).thenReturn(cartProductTypeVariation);

        CartProductTypeVariation result = cartProductTypeVariationService.addToCart(cart, productTypeVariation,
                quantity, varicode);

        assertNotNull(result);
        assertEquals(existingQuantity + quantity, result.getQuantity());
        verify(productTypeVariationService, times(1)).getById(productTypeVariation.getProdTypeVariId());
        verify(repository, times(1)).findByCartIdAndProdTypeVariId(cart, productTypeVariation);
        verify(repository, times(1)).save(cartProductTypeVariation);
    }

    @Test
    void testAddToCart_NullCart() {
        Integer quantity = 2;
        String varicode = "abc";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartProductTypeVariationService.addToCart(null, productTypeVariation, quantity, varicode));
        assertEquals("Cart cannot be null", exception.getMessage());
    }

    @Test
    void testAddToCart_NullCartId() {
        Integer quantity = 2;
        cart.setCartId(null);
        String varicode = "abc";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartProductTypeVariationService.addToCart(cart, productTypeVariation, quantity, varicode));
        assertEquals("Cart cannot be null", exception.getMessage());
    }

    @Test
    void testAddToCart_NullProductTypeVariation() {
        Integer quantity = 2;
        String varicode = "abc";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartProductTypeVariationService.addToCart(cart, null, quantity, varicode));
        assertEquals("ProductTypeVariation cannot be null", exception.getMessage());
    }

    @Test
    void testAddToCart_NullProductTypeVariationId() {
        Integer quantity = 2;
        productTypeVariation.setProdTypeVariId(null);
        String varicode = "abc";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartProductTypeVariationService.addToCart(cart, productTypeVariation, quantity, varicode));
        assertEquals("ProductTypeVariation cannot be null", exception.getMessage());
    }

    @Test
    void testAddToCart_InvalidQuantity() {
        Integer quantity = 0;
        String varicode = "abc";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartProductTypeVariationService.addToCart(cart, productTypeVariation, quantity, varicode));
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    @Test
    void testAddToCart_ProductTypeVariationNotFound() {
        Integer quantity = 2;
        String varicode = "abc";
        when(productTypeVariationService.getById(productTypeVariation.getProdTypeVariId())).thenReturn(null);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> cartProductTypeVariationService.addToCart(cart, productTypeVariation, quantity, varicode));
        assertEquals(AppCode.PRODUCT_TYPE_VARIATION_NOT_FOUND.getCode(), exception.getErrors().getCode());
        verify(productTypeVariationService, times(1)).getById(productTypeVariation.getProdTypeVariId());
    }

    @Test
    void testRemoveFromCart_Success() {
        Long cartProductTypeVariationId = 1L;

        when(repository.existsById(cartProductTypeVariationId)).thenReturn(true);
        doNothing().when(repository).deleteById(cartProductTypeVariationId);

        boolean result = cartProductTypeVariationService.removeFromCart(cartProductTypeVariationId);

        assertTrue(result);
        verify(repository, times(1)).existsById(cartProductTypeVariationId);
        verify(repository, times(1)).deleteById(cartProductTypeVariationId);
    }

    @Test
    void testRemoveFromCart_NotFound() {
        Long cartProductTypeVariationId = 1L;

        when(repository.existsById(cartProductTypeVariationId)).thenReturn(false);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> cartProductTypeVariationService.removeFromCart(cartProductTypeVariationId));
        assertEquals(AppCode.CART_NOT_FOUND.getCode(), exception.getErrors().getCode());
        verify(repository, times(1)).existsById(cartProductTypeVariationId);
    }

    @Test
    void testRemoveFromCart_InvalidId() {
        Long invalidId = 0L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartProductTypeVariationService.removeFromCart(invalidId));
        assertEquals("Invalid CartProductTypeVariation ID: " + invalidId, exception.getMessage());
    }

    @Test
    void testUpdateQuantity_Success() {
        Long cartProductTypeVariationId = 1L;
        Integer newQuantity = 5;

        when(repository.findById(cartProductTypeVariationId)).thenReturn(Optional.of(cartProductTypeVariation));
        when(repository.save(cartProductTypeVariation)).thenReturn(cartProductTypeVariation);

        CartProductTypeVariation result = cartProductTypeVariationService.updateQuantity(cartProductTypeVariationId,
                newQuantity);

        assertNotNull(result);
        assertEquals(newQuantity, result.getQuantity());
        verify(repository, times(1)).findById(cartProductTypeVariationId);
        verify(repository, times(1)).save(cartProductTypeVariation);
    }

    @Test
    void testUpdateQuantity_NotFound() {
        Long cartProductTypeVariationId = 1L;
        Integer newQuantity = 5;

        when(repository.findById(cartProductTypeVariationId)).thenReturn(Optional.empty());

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> cartProductTypeVariationService.updateQuantity(cartProductTypeVariationId, newQuantity));
        assertEquals(AppCode.CART_NOT_FOUND.getCode(), exception.getErrors().getCode());
        verify(repository, times(1)).findById(cartProductTypeVariationId);
    }

    @Test
    void testUpdateQuantity_InvalidId() {
        Long invalidId = 0L;
        Integer newQuantity = 5;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartProductTypeVariationService.updateQuantity(invalidId, newQuantity));
        assertEquals("Invalid CartProductTypeVariation ID: " + invalidId, exception.getMessage());
    }

    @Test
    void testUpdateQuantity_InvalidQuantity() {
        Long cartProductTypeVariationId = 1L;
        Integer invalidQuantity = 0;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartProductTypeVariationService.updateQuantity(cartProductTypeVariationId, invalidQuantity));
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }
}