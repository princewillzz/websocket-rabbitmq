package com.untanglechat.chatapp.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class InvalidOTPException extends RuntimeException {
    @Getter @Setter private String message;
}
