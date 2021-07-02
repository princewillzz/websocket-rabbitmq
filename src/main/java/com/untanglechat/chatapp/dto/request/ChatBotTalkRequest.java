package com.untanglechat.chatapp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ChatBotTalkRequest {
    
    @JsonProperty("textMessage")
    private String message;

}
