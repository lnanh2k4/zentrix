package com.zentrix.service;

import com.mservice.shared.exception.MoMoException;

import jakarta.servlet.http.HttpServletRequest;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date March 19, 2025
 */
public interface MomoService {
    public String createPayment(long totalAmount, String orderInfo, String username) throws MoMoException;

    public int verifyPayment(HttpServletRequest request) throws MoMoException;
}
