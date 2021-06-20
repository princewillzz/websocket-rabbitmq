package com.untanglechat.chatapp.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class UnAcceptableFormDataException extends RuntimeException {
    @Getter @Setter private String message;
}
