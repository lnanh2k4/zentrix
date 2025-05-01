package com.zentrix.model.exception;

import com.zentrix.model.response.ResponseObject;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  April 01, 2025
 */
public class AuthException extends BaseException {
    /**
     * This method allows to handle authentication exception
     * 
     * @param errorCode error code in app code
     */
    public AuthException(AppCode errorCode) {
        super(errorCode);
        error = new ResponseObject.Builder<AppCode>()
                .success(false)
                .message("AUTHORIZATION EXCEPTION:\t" + errorCode.getMessage())
                .code(errorCode.getCode())
                .build();
    }

    /**
     * This method allows to handle authentication exception
     * 
     * @param errorCode error code in app code
     * @param cause     exception cause
     */
    public AuthException(AppCode errorCode, Throwable cause) {
        super(errorCode, cause);
        error = new ResponseObject.Builder<AppCode>()
                .success(false)
                .message("AUTHORIZATION EXCEPTION:\t" + errorCode.getMessage())
                .code(errorCode.getCode())
                .build();
    }
}
