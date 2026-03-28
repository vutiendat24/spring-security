package com.springboot.spring_security.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.springboot.spring_security.DTO.res.APIResponse;



@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<APIResponse<Void>> handleAppException(AppException appException) {
        ErrorCode errorCode = appException.getErrorCode();

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(APIResponse.<Void>builder()
                        .status(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Void>> handleException(Exception exception) {
        ErrorCode errorCode = ErrorCode.UNDENFINED;

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(APIResponse.<Void>builder()
                        .status(errorCode.getCode())
                        .message(exception.getMessage())
                        .build());
    }
    
}
