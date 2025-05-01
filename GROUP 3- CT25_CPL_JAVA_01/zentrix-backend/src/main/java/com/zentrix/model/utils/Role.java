package com.zentrix.model.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  April 01, 2025
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum Role {
    GUEST(1, "Guest"),
    CUSTOMER(2, "Customer"),
    ADMIN(3, "Admin"),
    SELLER_STAFF(4, "Seller Staff"),
    WAREHOUSE_STAFF(5, "Warehouse Staff"),
    SHIPPER(6, "Shipper");

    int value;
    String roleName;
}
