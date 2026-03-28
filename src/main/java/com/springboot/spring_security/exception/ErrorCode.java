package com.springboot.spring_security.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

    UNDENFINED(9999,"Undefined ERROR",HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXIST(1000,"User already exists",HttpStatus.BAD_REQUEST),
    EMAIL_EXIST(1001,"Email already exists",HttpStatus.BAD_REQUEST),
    USERNAME_EXIST(1002,"Username already exists",HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1003,"Invalid password",HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1004,"Invalid email",HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1005,"Invalid username",HttpStatus.BAD_REQUEST),
    TOKEN_NOT_FOUND(1006,"Token not found",HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1007,"Token expired",HttpStatus.BAD_REQUEST),
    TOKEN_INVALID(1008,"Token invalid",HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1009,"Unauthorized",HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1010,"Forbidden",HttpStatus.FORBIDDEN),
    NOT_FOUND(1011,"Not found",HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR(1012,"Internal server error",HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
    
    ErrorCode(int code, String message, HttpStatus httpStatus){
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

}
