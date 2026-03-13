package com.springsecurity.springsecurity.Exception;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level= AccessLevel.PRIVATE, makeFinal=true)
public enum ErrorCode {
    USER_NOT_EXIST(2000,"USER NOT EXIST", HttpStatus.BAD_REQUEST ),
    EMAIL_ALREADY_EXISTS(2001,"EMAIL ALREADY EXISTS", HttpStatus.BAD_REQUEST ),
    INVALID_PASSWORD(2002,"INVALID PASSWORD", HttpStatus.BAD_REQUEST ),
    UNAUTHORIZED(2003,"UNAUTHORIZED", HttpStatus.UNAUTHORIZED ),
    INVALID_LOGIN(2004,"PASSWORD OR EMAIL IS INVALID", HttpStatus.BAD_REQUEST ),
    UNCATEGORIZED_EXCEPTION(9999,"UNCATEGORIZED EXCEPTION", HttpStatus.INTERNAL_SERVER_ERROR );


    int errorCode;
    String message;
    HttpStatus httpStatus;

    ErrorCode(int errorCode, String message, HttpStatus httpStatus) {
      this.errorCode = errorCode;
      this.message = message;
      this.httpStatus= httpStatus;
    }
}
