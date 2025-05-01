package com.zentrix.model.exception;

import com.zentrix.model.response.ResponseObject;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  April 01, 2025
 */
public class ActionFailedException extends BaseException {
    /**
     * This method allows to handle action failed exception
     * 
     * @param errorCode error code in app code
     */
    public ActionFailedException(AppCode errorCode) {
        super(errorCode);
        error = new ResponseObject.Builder<AppCode>()
                .success(false)
                .message("ACTION FAILED EXCEPTION:\t" + errorCode.getMessage())
                .code(errorCode.getCode())
                .build();
    }

    /**
     * This method allows to handle action failed exception
     * 
     * @param errorCode error code in app code
     * @param cause     exception cause
     */
    public ActionFailedException(AppCode errorCode, Throwable cause) {
        super(errorCode, cause);
        error = new ResponseObject.Builder<AppCode>()
                .success(false)
                .message("ACTION FAILED EXCEPTION:\t" + errorCode.getMessage())
                .code(errorCode.getCode())
                .build();
    }
}
