package com.untanglechat.chatapp.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormattedBeanValidationNameResponse {

    private List<FieldError> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static final class FieldError{
        private String message;
        private String code;
    }
    
}


