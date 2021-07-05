package com.untanglechat.chatapp.exceptionhandler;

import javax.validation.ConstraintViolationException;

import com.untanglechat.chatapp.dto.response.FormattedNameResponseMapper;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class BeanValidationFailExceptionHandler  {

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handlebeanValidationException(WebExchangeBindException ex) {

        return FormattedNameResponseMapper.fromWebExchangeBindException(ex);
    }
    

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handleConstraintViolationException(final ConstraintViolationException ex) {
        return FormattedNameResponseMapper.fromWebExchangeBindException(ex);
    } 

    
}
