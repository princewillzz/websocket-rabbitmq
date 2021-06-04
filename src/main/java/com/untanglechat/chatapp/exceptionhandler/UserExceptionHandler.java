package com.untanglechat.chatapp.exceptionhandler;

import com.untanglechat.chatapp.dto.response.ErrorResponse;
import com.untanglechat.chatapp.exceptions.UsernameAlreadyExists;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class UserExceptionHandler {

    @ExceptionHandler(UsernameAlreadyExists.class)
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse serverExceptionHandler(Exception ex) {
        log.error(ex.getMessage(),ex);
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }
}
