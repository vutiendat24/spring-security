package com.springsecurity.springsecurity.Exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.springsecurity.springsecurity.DTO.APIResponse;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. Bắt tất cả các lỗi chưa được định nghĩa (Internal Server Error)
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<APIResponse> handlingRuntimeException(Exception exception) {

        // Hiển thị lỗi trong console
        log.error("------------------ CRITICAL UNCAUGHT EXCEPTION ------------------");
        log.error("Error Detail: ", exception);
        log.error("------------------------------------------------------------------");

        APIResponse apiResponse = new APIResponse();
        apiResponse.setSuccess(false);
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getErrorCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_EXCEPTION.getHttpStatus()).body(apiResponse);
    }

    // 2. Bắt các lỗi do mình chủ động quăng ra (AppException)
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<APIResponse> handlingAppException(AppException exception) {
        log.warn("App Business Exception: {} - {}", exception.getErrorCode(), exception.getMessage());

        APIResponse apiResponse = new APIResponse();
        apiResponse.setSuccess(false);
        apiResponse.setCode(exception.getErrorCode());
        apiResponse.setMessage(exception.getMessage());

        return ResponseEntity.status(exception.getHttpStatus()).body(apiResponse);
    }

    // 3. Bắt các lỗi Validation (@Valid)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<APIResponse> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();
        log.warn("Validation Error: {}", enumKey);

        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            // Nếu message không phải là tên Enum, vẫn giữ log để debug
        }

        APIResponse apiResponse = new APIResponse();
        apiResponse.setSuccess(false);
        apiResponse.setCode(errorCode.getErrorCode());
        apiResponse.setMessage(enumKey);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
    }
}
