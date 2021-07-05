package com.untanglechat.chatapp.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.springframework.web.bind.support.WebExchangeBindException;

public class FormattedNameResponseMapper {

    private FormattedNameResponseMapper() {
    }

    public static FormattedBeanValidationNameResponse fromWebExchangeBindException(WebExchangeBindException ex) {
        FormattedBeanValidationNameResponse res = new FormattedBeanValidationNameResponse();
        List<FormattedBeanValidationNameResponse.FieldError> errors = ex.getFieldErrors().stream()
                .map(fieldError -> new FormattedBeanValidationNameResponse.FieldError(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());
        res.setErrors(errors);
        return res;
    }

    public static FormattedBeanValidationNameResponse fromWebExchangeBindException(ConstraintViolationException ex) {
        FormattedBeanValidationNameResponse res = new FormattedBeanValidationNameResponse();
        List<FormattedBeanValidationNameResponse.FieldError> errors = ex.getConstraintViolations().stream()
                .map(fieldError -> new FormattedBeanValidationNameResponse.FieldError(fieldError.getPropertyPath().toString(), fieldError.getMessage()))
                .collect(Collectors.toList());
        res.setErrors(errors);
        return res;
    }
  
}