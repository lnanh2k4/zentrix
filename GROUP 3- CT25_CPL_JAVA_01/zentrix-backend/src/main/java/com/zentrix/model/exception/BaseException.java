package com.zentrix.model.exception;

import com.zentrix.model.response.ResponseObject;

import lombok.extern.slf4j.Slf4j;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  April 01, 2025
 */
@Slf4j
public abstract class BaseException extends RuntimeException {
    protected ResponseObject<AppCode> error;

    /**
     * This method allows to handle base exception
     * 
     * @param errorCode error code in app code
     */
    public BaseException(AppCode errorCode) {
        super(errorCode.getMessage());
        log.info(errorCode.getCode() + ":\t" + errorCode.getMessage());
    }

    /**
     * This method allows to handle base exception
     * 
     * @param errorCode error code in app code
     * @param cause     exception cause
     */
    public BaseException(AppCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        log.info(errorCode.getMessage());
        log.error(errorCode.getMessage(), cause);
    }

    /**
     * This method allows to get errors
     * 
     * @return error
     */
    public ResponseObject<AppCode> getErrors() {
        return error;
    }
}
