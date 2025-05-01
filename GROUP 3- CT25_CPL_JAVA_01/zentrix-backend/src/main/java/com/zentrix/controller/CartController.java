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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 17, 2025
 */
@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart Controller", description = "APIs for managing user cart operations.")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {

        CartService cartService;
        CartProductTypeVariationService cartProductTypeVariationService;
        UserService userService;
        EmailService emailService;
        ProductTypeService productTypeService;

        /**
         * Retrieves the user's cart.
         *
         * @param userId The ID of the user whose cart is to be retrieved.
         * @param page   The page number for pagination (default: 0).
         * @param size   The number of items per page (default: 10).
         * @return ResponseEntity containing the user's cart.
         */
        @PreAuthorize("hasAnyRole('CUSTOMER') or hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get user's cart", description = "Retrieves the cart for a specific user with pagination.")
        @GetMapping
        public ResponseEntity<ResponseObject<List<Cart>>> getUserCart(
                        @RequestParam Long userId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                User user = userService.findUserByUserId(userId);
                if (user == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(new ResponseObject.Builder<List<Cart>>()
                                                        .content(null)
                                                        .message("User not found with ID: " + userId)
                                                        .code(HttpStatus.NOT_FOUND.value())
                                                        .success(false)
                                                        .build());
                }

                PaginationWrapper<List<Cart>> wrapper = cartService.getCartByUserId(user, page, size);
                ResponseObject<List<Cart>> response = new ResponseObject.Builder<List<Cart>>()
                                .unwrapPaginationWrapper(wrapper)
                                .message("Retrieved user's cart successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * Creates a new cart for the user.
         *
         * @param userId The ID of the user.
         * @return ResponseEntity with the created cart.
         */
        @PreAuthorize("hasAnyRole('CUSTOMER') or hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Create a new cart", description = "Creates a new cart for the user.")
        @PostMapping("/new")
        public ResponseEntity<ResponseObject<Cart>> createCart(
                        @RequestParam Long userId) {
                User user = userService.findUserByUserId(userId);
                if (user == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(new ResponseObject.Builder<Cart>()
                                                        .content(null)
                                                        .message("User not found with ID: " + userId)
                                                        .code(HttpStatus.NOT_FOUND.value())
                                                        .success(false)
                                                        .build());
                }

                try {
                        Cart cart = cartService.createCart(user);
                        ResponseObject<Cart> response = new ResponseObject.Builder<Cart>()
                                        .content(cart)
                                        .message("Cart created successfully!")
                                        .code(HttpStatus.CREATED.value())
                                        .success(true)
                                        .build();
                        return ResponseEntity.status(HttpStatus.CREATED).body(response);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(new ResponseObject.Builder<Cart>()
                                                        .content(null)
                                                        .message("Failed to create cart: " + e.getMessage())
                                                        .code(HttpStatus.BAD_REQUEST.value())
                                                        .success(false)
                                                        .build());
                }
        }

        /**
         * Adds a product to the user's cart.
         *
         * @param userId            The ID of the user.
         * @param cartId            The ID of the cart to add the product to.
         * @param productTypeVariId The ID of the ProductTypeVariation to add.
         * @param quantity          The quantity of the product to add.
         * @return ResponseEntity with the updated cart.
         */
        @PreAuthorize("hasAnyRole('CUSTOMER') or hasRole('ADMIN') or hasAnyRole('SELLER STAFF')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Add product to cart", description = "Adds a product to the user's cart.")
        @PostMapping("/add")
        public ResponseEntity<ResponseObject<Cart>> addToCart(
                        @RequestParam Long userId,
                        @RequestParam Long cartId,
                        @RequestParam Long productTypeVariId,
                        @RequestParam Integer quantity,
                        @RequestParam String variCode) {
                User user = userService.findUserByUserId(userId);
                if (user == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(new ResponseObject.Builder<Cart>()
                                                        .content(null)
                                                        .message("User not found with ID: " + userId)
                                                        .code(HttpStatus.NOT_FOUND.value())
                                                        .success(false)
                                                        .build());
                }

                try {
                        Cart updatedCart = cartService.addProductToCart(cartId, productTypeVariId, quantity, user,
                                        variCode);
                        ResponseObject<Cart> response = new ResponseObject.Builder<Cart>()
                                        .content(updatedCart)
                                        .message("Product added to cart successfully!")
                                        .code(HttpStatus.OK.value())
                                        .success(true)
                                        .build();
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(new ResponseObject.Builder<Cart>()
                                                        .content(null)
                                                        .message("Failed to add product to cart: " + e.getMessage())
                                                        .code(HttpStatus.BAD_REQUEST.value())
                                                        .success(false)
                                                        .build());
                }
        }

        /**
         * Retrieves the items in the user's cart.
         *
         * @param userId The ID of the user.
         * @param cartId The ID of the cart.
         * @return ResponseEntity containing the list of items in the cart.
         */
        @PreAuthorize("hasAnyRole('CUSTOMER') or hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get cart items", description = "Retrieves the list of items in the user's cart.")
        @GetMapping("/items")
        public ResponseEntity<ResponseObject<List<CartProductTypeVariation>>> getCartItems(
                        @RequestParam Long userId,
                        @RequestParam Long cartId) {
                User user = userService.findUserByUserId(userId);
                if (user == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(new ResponseObject.Builder<List<CartProductTypeVariation>>()
                                                        .content(null)
                                                        .message("User not found with ID: " + userId)
                                                        .code(HttpStatus.NOT_FOUND.value())
                                                        .success(false)
                                                        .build());
                }

                try {
                        List<CartProductTypeVariation> items = cartService.getCartItems(cartId, user);
                        ResponseObject<List<CartProductTypeVariation>> response = new ResponseObject.Builder<List<CartProductTypeVariation>>()
                                        .content(items)
                                        .message("Retrieved cart items successfully!")
                                        .code(HttpStatus.OK.value())
                                        .success(true)
                                        .build();
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(new ResponseObject.Builder<List<CartProductTypeVariation>>()
                                                        .content(null)
                                                        .message("Failed to retrieve cart items: " + e.getMessage())
                                                        .code(HttpStatus.BAD_REQUEST.value())
                                                        .success(false)
                                                        .build());
                }
        }

        /**
         * Updates the quantity of a product in the cart.
         *
         * @param userId                     The ID of the user.
         * @param cartProductTypeVariationId The ID of the CartProductTypeVariation to
         *                                   update.
         * @param quantity                   The new quantity of the product.
         * @return ResponseEntity with the updated CartProductTypeVariation.
         */
        @PreAuthorize("hasAnyRole('CUSTOMER') or hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Update product quantity in cart", description = "Updates the quantity of a product in the user's cart.")
        @PutMapping("/update-quantity")
        public ResponseEntity<ResponseObject<CartProductTypeVariation>> updateCartQuantity(
                        @RequestParam Long userId,
                        @RequestParam Long cartProductTypeVariationId,
                        @RequestParam Integer quantity) {
                User user = userService.findUserByUserId(userId);
                if (user == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(new ResponseObject.Builder<CartProductTypeVariation>()
                                                        .content(null)
                                                        .message("User not found with ID: " + userId)
                                                        .code(HttpStatus.NOT_FOUND.value())
                                                        .success(false)
                                                        .build());
                }

                try {
                        CartProductTypeVariation updatedItem = cartProductTypeVariationService
                                        .updateQuantity(cartProductTypeVariationId, quantity);

                        ResponseObject<CartProductTypeVariation> response = new ResponseObject.Builder<CartProductTypeVariation>()
                                        .content(updatedItem)
                                        .message("Updated product quantity in cart successfully!")
                                        .code(HttpStatus.OK.value())
                                        .success(true)
                                        .build();
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(new ResponseObject.Builder<CartProductTypeVariation>()
                                                        .content(null)
                                                        .message("Failed to update product quantity: " + e.getMessage())
                                                        .code(HttpStatus.BAD_REQUEST.value())
                                                        .success(false)
                                                        .build());
                }
        }

        /**
         * Removes a product from the user's cart.
         *
         * @param userId                     The ID of the user.
         * @param cartProductTypeVariationId The ID of the CartProductTypeVariation to
         *                                   remove.
         * @return ResponseEntity indicating the result of the removal.
         */
        @PreAuthorize("hasAnyRole('CUSTOMER') or hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Remove product from cart", description = "Removes a product from the user's cart.")
        @DeleteMapping("/remove")
        public ResponseEntity<ResponseObject<Void>> removeFromCart(
                        @RequestParam Long userId,
                        @RequestParam Long cartProductTypeVariationId) {
                User user = userService.findUserByUserId(userId);
                if (user == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(new ResponseObject.Builder<Void>()
                                                        .message("User not found with ID: " + userId)
                                                        .code(HttpStatus.NOT_FOUND.value())
                                                        .success(false)
                                                        .build());
                }

                boolean success = cartProductTypeVariationService.removeFromCart(cartProductTypeVariationId);
                ResponseObject<Void> response = new ResponseObject.Builder<Void>()
                                .message("Removed product from cart successfully!")
                                .code(HttpStatus.OK.value())
                                .success(success)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * Clears all items from the user's cart.
         *
         * @param userId The ID of the user.
         * @param cartId The ID of the cart to clear.
         * @return ResponseEntity indicating the result of the operation.
         */
        @PreAuthorize("hasAnyRole('CUSTOMER') or hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Clear cart", description = "Removes all items from the user's cart.")
        @DeleteMapping("/clear")
        public ResponseEntity<ResponseObject<Void>> clearCart(
                        @RequestParam Long userId,
                        @RequestParam Long cartId) {
                User user = userService.findUserByUserId(userId);
                if (user == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(new ResponseObject.Builder<Void>()
                                                        .message("User not found with ID: " + userId)
                                                        .code(HttpStatus.NOT_FOUND.value())
                                                        .success(false)
                                                        .build());
                }

                try {
                        cartService.clearCart(cartId, user);
                        ResponseObject<Void> response = new ResponseObject.Builder<Void>()
                                        .message("Cart cleared successfully!")
                                        .code(HttpStatus.OK.value())
                                        .success(true)
                                        .build();
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(new ResponseObject.Builder<Void>()
                                                        .message("Failed to clear cart: " + e.getMessage())
                                                        .code(HttpStatus.BAD_REQUEST.value())
                                                        .success(false)
                                                        .build());
                }
        }

        /**
         * Sends an email with the large quantity request details to a predefined email
         * address.
         *
         * @param companyName   The name of the company.
         * @param customerName  The name of the customer.
         * @param phoneNumber   The phone number of the customer.
         * @param email         The email address of the customer.
         * @param productTypeId The ID of the product type.
         * @param quantity      The requested quantity.
         * @param note          Additional notes from the customer.
         * @return ResponseEntity indicating the result of the email sending operation.
         */
        @Operation(summary = "Send large quantity request via email", description = "Sends the large quantity request details to a predefined email address without saving to the database.")
        @PostMapping("/send-large-quantity-request")
        public ResponseEntity<ResponseObject<Void>> sendLargeQuantityRequest(
                        @RequestParam String companyName,
                        @RequestParam String customerName,
                        @RequestParam String phoneNumber,
                        @RequestParam String email,
                        @RequestParam Long productTypeId,
                        @RequestParam Integer quantity,
                        @RequestParam(required = false) String note) {
                try {
                        // Get ProductType information based on productTypeId
                        ProductType productType = productTypeService.findProductTypeById(productTypeId);
                        String productTypeName = productType.getProdTypeName() != null
                                        ? productType.getProdTypeName()
                                        : "Unknown Product";

                        // Permanent email address to send information
                        String to = "vuxblack@gmail.com";
                        String cc = null;
                        String bcc = null;
                        String subject = "Bulk order from customer: " + customerName;

                        // Create email content in HTML with nice interface
                        StringBuilder emailContent = new StringBuilder();
                        emailContent.append("<html><body>");
                        emailContent.append(
                                        "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 20px auto; border: 1px solid #e0e0e0; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); background-color: #ffffff;'>");

                        // Main title
                        emailContent.append(
                                        "<div style='background-color: #2e6c80; color: #ffffff; padding: 20px; text-align: center; border-radius: 10px 10px 0 0;'>");
                        emailContent.append(
                                        "<h1 style='margin: 0; font-size: 28px; font-weight: bold;'>Zentrix Store</h1>");
                        emailContent.append(
                                        "<p style='margin: 5px 0 0; font-size: 14px; opacity: 0.8;'>Your Trusted Tech Partner</p>");
                        emailContent.append("</div>");

                        // Request notification
                        emailContent.append(
                                        "<div style='padding: 20px; background-color: #f5f7fa; text-align: center;'>");
                        emailContent.append(
                                        "<h2 style='color: #2e6c80; font-size: 22px; margin: 0; font-weight: 600;'>New Large Quantity Request</h2>");
                        emailContent.append("</div>");

                        // Customer information
                        emailContent.append("<div style='padding: 20px 25px;'>");
                        emailContent.append(
                                        "<h3 style='color: #333333; font-size: 18px; margin: 0 0 10px; font-weight: 600;'>Customer Information</h3>");
                        emailContent.append(
                                        "<p style='margin: 5px 0; font-size: 15px; color: #555555; line-height: 1.5;'><strong style='color: #333333;'>Company Name:</strong> ")
                                        .append(companyName).append("</p>");
                        emailContent.append(
                                        "<p style='margin: 5px 0; font-size: 15px; color: #555555; line-height: 1.5;'><strong style='color: #333333;'>Customer Name:</strong> ")
                                        .append(customerName).append("</p>");
                        emailContent.append(
                                        "<p style='margin: 5px 0; font-size: 15px; color: #555555; line-height: 1.5;'><strong style='color: #333333;'>Phone Number:</strong> ")
                                        .append(phoneNumber).append("</p>");
                        emailContent.append(
                                        "<p style='margin: 5px 0; font-size: 15px; color: #555555; line-height: 1.5;'><strong style='color: #333333;'>Email:</strong> <a href='mailto:")
                                        .append(email).append("' style='color: #2e6c80; text-decoration: none;'>")
                                        .append(email).append("</a></p>");
                        emailContent.append("</div>");

                        // Request information table
                        emailContent.append("<div style='padding: 20px 25px; border-top: 1px solid #e0e0e0;'>");
                        emailContent.append(
                                        "<h3 style='color: #333333; font-size: 18px; margin: 0 0 10px; font-weight: 600;'>Request Details</h3>");
                        emailContent.append(
                                        "<table style='border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; font-size: 14px; color: #555555;'>");

                        // Table title
                        emailContent.append("<thead>");
                        emailContent.append("<tr style='background-color: #e6f7ff; color: #333333;'>");
                        emailContent.append(
                                        "<th style='padding: 12px 15px; text-align: left; font-weight: 600; border-bottom: 1px solid #e0e0e0; width: 40%;'>Field</th>");
                        emailContent.append(
                                        "<th style='padding: 12px 15px; text-align: left; font-weight: 600; border-bottom: 1px solid #e0e0e0; width: 60%;'>Details</th>");
                        emailContent.append("</tr>");
                        emailContent.append("</thead>");

                        // Table content
                        emailContent.append("<tbody>");
                        emailContent.append("<tr style='background-color: #fafafa;'>");
                        emailContent.append(
                                        "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Product Type</td>");
                        emailContent.append(
                                        "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; color: #2e6c80; font-weight: 500;'>")
                                        .append(productTypeName).append("</td>");
                        emailContent.append("</tr>");
                        emailContent.append("<tr>");
                        emailContent.append(
                                        "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Requested Quantity</td>");
                        emailContent.append(
                                        "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; font-weight: 500;'>")
                                        .append(quantity).append("</td>");
                        emailContent.append("</tr>");
                        emailContent.append("<tr style='background-color: #fafafa;'>");
                        emailContent.append(
                                        "<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>Note</td>");
                        emailContent.append("<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0;'>")
                                        .append(note != null ? note : "No note provided").append("</td>");
                        emailContent.append("</tr>");
                        emailContent.append("</tbody>");
                        emailContent.append("</table>");
                        emailContent.append("</div>");

                        // Thanks
                        emailContent.append(
                                        "<div style='padding: 20px 25px; text-align: center; border-top: 1px solid #e0e0e0; background-color: #f5f7fa; border-radius: 0 0 10px 10px;'>");
                        emailContent.append(
                                        "<p style='margin: 0 0 10px; font-size: 14px; color: #555555; line-height: 1.5;'>Please review the request and contact the customer as soon as possible.</p>");
                        emailContent.append(
                                        "<p style='margin: 0; font-size: 14px; color: #333333; font-weight: bold;'>Best regards,<br>The Zentrix Team</p>");
                        emailContent.append(
                                        "<p style='margin: 10px 0 0; font-size: 12px; color: #888888;'>Hotline: 0123 456 789 | Email: <a href='mailto:support@zentrix.com' style='color: #2e6c80; text-decoration: none;'>support@zentrix.com</a></p>");
                        emailContent.append("</div>");

                        // End of frame
                        emailContent.append("</div>");
                        emailContent.append("</body></html>");

                        // Send email
                        emailService.sendEmailWithHtmlBody(to, cc, bcc, subject, emailContent.toString());

                        ResponseObject<Void> response = new ResponseObject.Builder<Void>()
                                        .message("Your request has been emailed successfully!")
                                        .code(HttpStatus.OK.value())
                                        .success(true)
                                        .build();
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(new ResponseObject.Builder<Void>()
                                                        .message("Unable to send request email: " + e.getMessage())
                                                        .code(HttpStatus.BAD_REQUEST.value())
                                                        .success(false)
                                                        .build());
                }
        }
}