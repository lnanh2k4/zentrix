package com.zentrix.model.exception;

import java.io.File;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  February 12, 2025
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AppCode {
        /*
         * !Error code for Entity Attribute: 1000 - 1099 Branch: 1100 - 1199 Cart: 1200
         * - 1299 Category: 1300 - 1399
         * 
         * Image, Image Post, Image Product Type, Image Review: 1400 - 1499 Menbership:
         * 1500 - 1599 Notification, Notification Role: 1600 - 1699 Order, Order Detail:
         * 1700 - 1799 Post: 1800 - 1899
         * 
         * Product, Product Type, Product Type Attribute, Product Type Branch: Product
         * Type Variation 1900 - 1999
         * 
         * Promotion: 2000 - 2099 Review: 2100 - 2199 Role: 2200 - 2299 Staff: 2300 -
         * 2399 Stock, Stock Detail: 2400 - 2499 Supplier: 2500 - 2599 User, User
         * Promotion, User Role: 2600 - 2699 Variation: 2700 - 2799 Warranty: 2800 -
         * 2899 Orthers: 2900 - 2999
         */

        // Error code for ROLE entity [2200 - 22999]
        ROLE_NOT_FOUND(2200, "Role Exception: Role not found"),
        // Error code for STAFF entity [2300 - 2399]
        STAFF_NOT_FOUND(2300, "Staff Exception: Staff not found"),
        STAFF_EXISTED(2301, "Staff Exception: Staff existed"),

        // Error code for branch entity [1100 - 1199]
        BRANCH_NOT_FOUND(1100, "Branch Exception: Branch not found"),
        BRANCH_ALREADY_EXISTS(1101, "Branch Exception: Branch already exists"),
        BRANCH_CREATION_FAILED(1102, "Branch Exception: Failed to create new branch"),
        BRANCH_UPDATE_FAILED(1103, "Branch Exception: Failed to update branch"),
        BRANCH_DELETE_FAILED(1104, "Branch Exception: Failed to delete branch"),
        BRANCH_INTERNAL_ERROR(1105, "Branch Exception: Internal server error while processing branch"),
        BRANCH_CONTAINS_PRODUCTS(1110, "Cannot delete branch because it contains products"),

        // Error code for category entity [1300 - 1399]
        CATEGORY_NOT_FOUND(1300, "Category Exception: Category not found"),
        CATEGORY_EXISTED(1301, "Category Exception: Category already exists"),
        CATEGORY_CREATION_FAILED(1302, "Category Exception: Failed to create new category"),
        CATEGORY_UPDATE_FAILED(1303, "Category Exception: Failed to update category"),
        CATEGORY_DELETE_FAILED(1304, "Category Exception: Failed to delete category"),
        CATEGORY_INTERNAL_ERROR(1305, "Category Exception: Internal server error while processing category"),
        INVALID_CATEGORY_NAME(1306, "Category Exception: Invalid category name"),
        CATEGORY_PARENT_NOT_FOUND(1307, "Category Exception: Parent category not found"),
        CATEGORY_HAS_SUBCATEGORIES(1308, "Category Exception: Cannot delete category with existing subcategories"),
        CATEGORY_PARENT_EXISTS(1309, "A category cannot be its own parent."),

        // Error code for supplier entity [2500 - 2599]
        SUPPLIER_NOT_FOUND(2500, "Supplier Exception: Supplier not found"),
        SUPPLIER_EXISTED(2501, "Supplier Exception: Supplier already exists"),
        SUPPLIER_UPDATE_FAILED(2502, "Supplier Exception: Failed to update supplier"),
        SUPPLIER_DELETE_FAILED(2503, "Supplier Exception: Failed to delete supplier"),
        SUPPLIER_CREATION_FAILED(2504, "Supplier Exception: Failed to create new supplier"),
        SUPPLIER_INTERNAL_ERROR(2505, "Supplier Exception: Internal server error while processing supplier"),

        // Error code for user entity [2600 - 2699]

        // Error code for USER entity [2600 - 2633]
        USER_NOT_FOUND(2600, "User Exception: User not found"), USER_EXISTED(2601, "User Exception: User existed"),
        USERNAME_EXISTED(2602, "User Exception: Username already exists in other account"),
        USERNAME_DELETED(2603, "User Exception: Username already deleted"),
        INVALID_USERNAME(2604, "User Exception: Invalid username"),
        INVALID_PASSWORD(2605, "User Exception: Invalid password"),
        USER_GET_LIST_FAILED(2606, "User Exception: Cannot get list of users"),
        USER_UPDATED_FAILED(2607, "User Exception: User update failed"),
        INVALID_USER_ID(2608, "User Exception: Invalid user id or not exist"),
        EMAIL_NOT_FOUND(2609, "User Exception: Email not found"),
        EMAIL_EXISTED(2610, "User Exception: Email already existed"),
        PHONE_NOT_FOUND(2611, "User Exception: Phone Number not found"),
        PHONE_EXISTED(2612, "User Exception: Phone Number already existed"),
        // Error code for USER ROLE entity [2634 - 2666]
        USER_ROLE_NOT_FOUND(2634, "User Exception: User not found"),
        USER_ROLE_EXISTED(2634, "User Exception: User not found"),
        USER_NOT_AUTHORIZED(2635, "User Exception: User not authorized"),
        // Error code for USER PROMOTION entity [2667 - 2699]
        USER_PROMOTION_NOT_FOUND(2667, "User Exception: User promotion not found"),
        USER_PROMOTION_CLAIM_SUCCESSFUL(2667, "User Response: User promotion notclaim successful"),

        // Error code for post entity [1800 - 1899]
        POST_NOT_FOUND(1800, "Post Exception: Post not found"),
        POST_UPDATE_FAILED(1801, "Post Exception: Failed to update post"),
        POST_DELETE_FAILED(1802, "Post Exception: Failed to delete post"),
        POST_CREATION_FAILED(1803, "Post Exception: Failed to create new post"),
        POST_INTERNAL_ERROR(1804, "Post Exception: Internal server error while processing post"),
        POST_TITLE_REQUIRED(1805, "Post Validation: Title must not be empty"),
        POST_TITLE_TOO_LONG(1806, "Post Validation: Title must not exceed 255 characters"),
        POST_DESCRIPTION_BLANK(1807, "Post Validation: Description must not be blank"),
        POST_CREATED_AT_IN_FUTURE(1808, "Post Validation: Created date cannot be in the future"),

        // Error code for promotion entity [2000 - 2099]
        PROMOTION_NOT_FOUND(2000, "Promotion Exception: Promotion not found"),
        PROMOTION_CREATION_FAILED(2001, "Promotion Exception: Failed to create new promotion"),
        PROMOTION_UPDATE_FAILED(2002, "Promotion Exception: Failed to update promotion"),
        PROMOTION_DELETE_FAILED(2003, "Promotion Exception: Failed to delete promotion"),
        PROMOTION_INTERNAL_ERROR(2004, "Promotion Exception: Internal server error while processing promotion"),
        PROMOTION_FOUND(2005, "Promotion Response: Promotion  found"),
        PROMOTION_CREATION_SUCCESSFUL(2006, "Promotion Response: Create new promotion successful"),
        PROMOTION_UPDATE_SUCCESSFUL(2007, "Promotion Response: Update promotion successful"),
        PROMOTION_DELETE_SUCCESSFUL(2008, "Promotion Response: Delete promotion successful"),
        PROMOTION_GET_LIST_FAILED(2009, "Promotion Exception: Promotion get list failed"),
        INVALID_PROMOTION_ID(2010, "Promotion Exception: Promotion id is invalid or missing"),
        INVALID_PROMOTION_NAME(2011, "Promotion Exception: Promotion name is invalid or missing"),
        INVALID_PROMOTION_CODE(2012, "Promotion Exception: Promotion code is invalid or missing"),
        INVALID_DISCOUNT(2013, "Promotion Exception: Discount value is invalid"),
        INVALID_DATE_RANGE(2014, "Promotion Exception: Invalid date range (start date must be before end date)"),
        INVALID_PROMOTION_QUANTITY(2015, "Promotion Exception: Promotion quantity is invalid"),
        INVALID_PROMOTION_STATUS(2016, "Promotion Exception: Promotion status is invalid"),
        INVALID_FILTER_COMBINATION(2017, "Promotion Exception: Promotion filter is invalid"),
        PROMOTION_OUT_OF_STOCK(2018, "Promotion Exception: Promotion is out of stock or invalid quantity"),
        // Error code for review entity [2100 - 2199]
        REVIEW_NOT_FOUND(2100, "Review Exception: Review not found"),
        REVIEW_CREATION_FAILED(2101, "Review Exception: Failed to create new review"),
        REVIEW_UPDATE_FAILED(2102, "Review Exception: Failed to update review"),
        REVIEW_DELETE_FAILED(2103, "Review Exception: Failed to delete review"),
        REVIEW_INTERNAL_ERROR(2104, "Review Exception: Internal server error while processing post"),
        REVIEW_RATING_REQUIRED(2105, "Review Validation: Rating must not be null"),
        REVIEW_RATING_INVALID_RANGE(2106, "Review Validation: Rating must be between 1 and 5"),
        REVIEW_COMMENT_REQUIRED(2107, "Review Validation: Comment must not be blank"),
        REVIEW_CREATED_AT_REQUIRED(2108, "Review Validation: Created date must not be null"),
        REVIEW_CREATED_AT_IN_FUTURE(2109, "Review Validation: Created date cannot be in the future"),

        // Error code for membership entity [1500 - 1599]
        MEMBERSHIP_NOT_FOUND(1500, "Membership Exception: Membership not found"),
        MEMBERSHIP_DELETE_FAILED(1501, "Membership Exception:  Failed to delete membership"),
        MEMBERSHIP_UPDATE_FAILED(1502, "Membership Exception: Failed to update membership"),
        MEMBERSHIP_CREATION_FAILED(1503, "Membership Exception: Failed to create membership"),
        MEMBERSHIP_INTERNAL_ERROR(1504, "Membership Exception: Internal server error while processing membership"),

        // Error code for product entity [1400 - 1499]
        IMAGE_NOT_FOUND(1400, "Image Exception: Image not found"),
        IMAGE_UPDATE_FAILED(1401, "Image Exception: Failed to update image"),
        IMAGE_DELETE_FAILED(1402, "Image Exception: Failed to delete image"),
        IMAGE_CREATE_FAILED(1403, "Image Exception: Failed to create image"),

        IMAGE_PRODUCT_TYPE_NOT_FOUND(1404, "Image Product Type Exception: Image Product Type not found"),
        IMAGE_PRODUCT_TYPE_UPDATE_FAILED(1405, "Image Product Type Exception: Failed to update image product type"),
        IMAGE_PRODUCT_TYPE_DELETE_FAILED(1406, "Image Product Type Exception: Failed to delete image product type"),
        IMAGE_PRODUCT_TYPE_CREATE_FAILED(1407, "Image Product Type Exception: Failed to create image product type"),

        // Error code for product entity [2700 - 2799]
        VARIATION_NOT_FOUND(2700, "Variation Exception: Attribute not found"),
        VARIATION_UPDATE_FAILED(2701, "Attribute Exception: Failed to update attribute"),
        VARIATION_DELETE_FAILED(2702, "Attribute Exception: Failed to delete attribute"),
        VARIATION_CREATE_FAILED(2703, "Attribute Exception: Failed to create attribute"),
        // Error code for product entity [1000 - 1099]
        ATTRIBUTE_NOT_FOUND(1900, "Attribute Exception: Attribute not found"),
        ATTRIBUTE_UPDATE_FAILED(1901, "Attribute Exception: Failed to update attribute"),
        ATTRIBUTE_DELETE_FAILED(1902, "Attribute Exception: Failed to delete attribute"),
        ATTRIBUTE_CREATE_FAILED(1903, "Attribute Exception: Failed to create attribute"),
        // Error code for product entity [1900 - 1999]
        PRODUCT_NOT_FOUND(1900, "Product Exception: Product not found"),
        PRODUCT_UPDATE_FAILED(1901, "Product Exception: Failed to update product"),
        PRODUCT_DELETE_FAILED(1902, "Product Exception: Failed to delete product"),
        PRODUCT_CREATE_FAILED(1903, "Product Exception: Failed to create product"),

        PRODUCT_TYPE_NOT_FOUND(1904, "Product Type Exception: Product Type not found"),
        PRODUCT_TYPE_UPDATE_FAILED(1905, "Product Type Exception: Failed to update product type"),
        PRODUCT_TYPE_DELETE_FAILED(1906, "Product Type Exception: Failed to delete product type"),
        PRODUCT_TYPE_CREATE_FAILED(1907, "Product Type Exception: Failed to create product type"),

        PRODUCT_TYPE_BRANCH_NOT_FOUND(1908, "Product Type Branch Exception: Product Type Branch not found"),
        PRODUCT_TYPE_BRANCH_UPDATE_FAILED(1909, "Product Type Branch Exception: Failed to update product type branch"),
        PRODUCT_TYPE_BRANCH_DELETE_FAILED(1910, "Product Type Branch Exception: Failed to delete product type branch"),
        PRODUCT_TYPE_BRANCH_CREATE_FAILED(1911, "Product Type Branch Exception: Failed to create product type branch"),
        // Error code for review cart [1200 - 1299]
        CART_NOT_FOUND(1500, "Cart Exception: Cart not found"),
        CART_DELETE_FAILED(1501, "Cart Exception:  Failed to delete cart"),
        CART_UPDATE_FAILED(1502, "Cart Exception: Failed to update cart"),
        CART_CREATION_FAILED(1503, "Cart Exception: Failed to create cart"),
        CART_INTERNAL_ERROR(1504, "Cart Exception: Internal server error while processing cart"),
        // Error code for review order [1700 - 1799]
        ORDER_UPDATE_FAILED(1700, "Order Exception: Failed to update order"),
        ORDER_NOT_FOUND(1701, "Order Exception: Failed not found "),
        ORDER_DETAIL_CREATE_FAILED(1702, "OrderDetail Exception: Failed to create order detail"),
        ORDER_DETAIL_DELETE_FAILED(1703, "OrderDetail Exception:  Failed to delete order detail"),
        ORDER_DETAIL_NOT_FOUND(1704, "OrderDetail Exception: Failed not found"),
        ORDER_DETAIL_UPDATE_FAILED(1705, "OrderDetail Exception: Failed to update order detail"),

        PRODUCT_TYPE_ATTRIBUTE_NOT_FOUND(1912, "Product Type Attribute Exception: Product Type Attribute not found"),
        PRODUCT_TYPE_ATTRIBUTE_UPDATE_FAILED(1913,
                        "Product Type Attribute Exception: Failed to update product type attribute"),
        PRODUCT_TYPE_ATTRIBUTE_DELETE_FAILED(1914,
                        "Product Type Attribute Exception: Failed to delete product type attribute"),
        PRODUCT_TYPE_ATTRIBUTE_CREATE_FAILED(1915,
                        "Product Type Attribute Exception: Failed to create product type attribute"),

        PRODUCT_TYPE_VARIATION_NOT_FOUND(1916, "Product Type Variation Exception: Product Type Variation not found"),
        PRODUCT_TYPE_VARIATION_UPDATE_FAILED(1917,
                        "Product Type Variation Exception: Failed to update product type variation"),
        PRODUCT_TYPE_VARIATION_DELETE_FAILED(1918,
                        "Product Type Variation Exception: Failed to delete product type variationh"),
        PRODUCT_TYPE_VARIATION_CREATE_FAILED(1919,
                        "Product Type Variation Exception: Failed to create product type variation"),

        // Error code for warranty entity [2800 - 2899]
        WARRANTY_NOT_FOUND(2800, "Warranty Exception: warranty not found"),
        WARRANTY_CREATION_FAILED(2801, "Warranty Exception: Failed to create new warranty"),
        WARRANTY_UPDATE_FAILED(2802, "Warranty Exception: Failed to update warranty"),
        WARRANTY_DELETE_FAILED(2803, "Warranty Exception: Failed to delete warranty"),
        WARRANTY_INTERNAL_ERROR(2804, "Warranty Exception: Internal server error while processing warranty"),

        STOCK_NOT_FOUND(2400, "Stock Exception: Stock not found"),
        STOCK_UPDATE_FAILED(2401, "Stock Exception: Failed to update Stock"),
        STOCK_DELETE_FAILED(2402, "Stock Exception: Failed to delete Stock"),
        STOCK_CREATE_FAILED(2403, "Stock Exception: Failed to create Stock"),

        STOCK_DETAIL_NOT_FOUND(2404, "StockDetail Exception: StockDetail not found"),
        STOCK_DETAIL_UPDATE_FAILED(2405, "StockDetail Exception: Failed to update StockDetail"),
        STOCK_DETAIL_DELETE_FAILED(2406, "StockDetail Exception: Failed to delete StockDetail"),
        STOCK_DETAIL_CREATE_FAILED(2407, "StockDetail Exception: Failed to create StockDetail"),

        // Error code for review notification [1600 - 1699]
        NOTIFICATION_NOT_FOUND(1600, "Notification Exception: Notification not found"),
        NOTIFICATION_CREATE_FAILED(1601, "Notification Exception: Failed to create notification"),
        NOTIFICATION_DELETE_FAILED(1602, "Notification Exception: Failed to delete notification"),
        NOTIFICATION_SEARCH_FAILED(1603, "Notification Exception: Failed to search notifications"),
        NOTIFICATION_PERMISSION_DENIED(1604, "Notification Exception: Permission denied to access notification"),
        NOTIFICATION_CREATOR_REQUIRED(1605, "Notification Validation: Creator must not be null"),
        NOTIFICATION_CREATOR_INVALID(1606, "Notification Validation: Creator ID must be greater than 0"),
        NOTIFICATION_TITLE_BLANK(1607, "Notification Validation: Title cannot be blank"),
        NOTIFICATION_TITLE_INVALID_LENGTH(1608, "Notification Validation: Title must be between 5 and 100 characters"),
        NOTIFICATION_DESCRIPTION_BLANK(1609, "Notification Validation: Description cannot be blank"),
        NOTIFICATION_DESCRIPTION_INVALID_LENGTH(1610,
                        "Notification Validation: Description must be between 10 and 500 characters"),
        NOTIFICATION_CREATED_AT_REQUIRED(1611, "Notification Validation: Created date must not be null"),
        NOTIFICATION_CREATED_AT_IN_FUTURE(1612, "Notification Validation: Created date cannot be in the future"),
        NOTIFICATION_STATUS_REQUIRED(1613, "Notification Validation: Status must not be null"),
        NOTIFICATION_STATUS_INVALID(1614, "Notification Validation: Status must be 0 (inactive) or 1 (active)"),

        // Error code for others [2900 - 2999]
        EMAIL_SENT_FAILED(2900, "Email Exception: Email is sent failed"),
        MESSAGE_SETED_FAILED(2901, "Message Exeption: Message is seted failed"),

        // Error code for upload file [2900 - 2999]
        FILE_UPLOAD_ERROR(2900, "File Exception: Can not upload file"),
        INVALID_FILE_NULL(2901, "File Exception: File not found"),
        INVALID_INPUT(2902, "Input Exception: Invalid or missing input data."),
        FIELD_NOT_VALID(2903, "Validation Exception: One or more fields contain invalid values."),
        GOOGLE_TOKEN_INVALID(2904, "Validation Exception: Invalid Google Token"),
        CURRENT_PASSWORD_INCORRECT(2905, "Validation Exception: Current password is incorrect"),
        OTP_INVALID(2906, "Validation Exception: OTP is invalid"),
        OTP_EXPIRED(2907, "Validation Exception: OTP is expired"),
        TOKEN_INVALID(2908, "Validation Exception: Token is invalid"),
        TOKEN_EXPIRED(2909, "Validation Exception: Token is expired");

        int code;
        String message;

}
