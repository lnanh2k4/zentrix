package com.zentrix.model.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  February 12, 2025
 */
@RequiredArgsConstructor
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum Status {
    UNACTIVE(0),
    ACTIVE(1),
    VERIFYING(2),
    LOCK(3),
    LOCK_VERIFY(4);

    int value;
}
