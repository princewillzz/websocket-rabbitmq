package com.untanglechat.chatapp.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsernameAlreadyExists extends RuntimeException {
    
    private String message;

}
