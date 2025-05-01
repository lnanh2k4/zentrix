package com.zentrix.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.zentrix.model.response.ResponseObject;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  February 12, 2025
 */

@ControllerAdvice
public class GlobalExceptionHandler {

        /**
         * This method allows to handle validation failed exception
         * 
         * @param ex validation failed exception
         * @return a string
         */
        @ExceptionHandler(ValidationFailedException.class)
        public ResponseEntity<ResponseObject<String>> handleValidationFailedException(ValidationFailedException ex) {
                ResponseObject<String> response = new ResponseObject.Builder<String>()
                                .content(ex.getMessage())
                                .message("VALIDATION FAILED EXCEPTION")
                                .code(HttpStatus.BAD_REQUEST.value())
                                .success(false)
                                .build();
                return ResponseEntity.status(400)
                                .body(response);
        }

        /**
         * This method allows to handle action failed exception
         * 
         * @param ex action failed exception
         * @return a string
         */
        @ExceptionHandler(ActionFailedException.class)
        public ResponseEntity<ResponseObject<String>> handleActionFailedException(ActionFailedException ex) {
                ResponseObject<String> response = new ResponseObject.Builder<String>()
                                .content(ex.getMessage())
                                .message("ACTION FAILED EXCEPTION")
                                .code(HttpStatus.BAD_REQUEST.value())
                                .success(false)
                                .build();
                return ResponseEntity.status(400)
                                .body(response);
        }

        /**
         * This method allows to handle base exception
         * 
         * @param ex base exception
         * @return a string
         */
        @ExceptionHandler(BaseException.class)
        public ResponseEntity<ResponseObject<String>> handleBaseException(BaseException ex) {
                ResponseObject<String> response = new ResponseObject.Builder<String>()
                                .content(ex.getMessage())
                                .message(ex.getClass().getSimpleName())
                                .code(HttpStatus.BAD_REQUEST.value())
                                .success(false)
                                .build();
                return ResponseEntity.status(400)
                                .body(response);
        }

}
