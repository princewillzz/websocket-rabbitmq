package com.untanglechat.chatapp.dto;

import java.util.Date;
import java.util.UUID;

import lombok.Data;


@Data
public class MessageDTO {

    private final String id;
    private String message;
    private String type;

    private final Date sentTime;
    private String sentBy;
    private String sentTo;

    public MessageDTO() {
        this.sentTime = new Date();
        this.id = UUID.randomUUID().toString();
    }

    
    
}
