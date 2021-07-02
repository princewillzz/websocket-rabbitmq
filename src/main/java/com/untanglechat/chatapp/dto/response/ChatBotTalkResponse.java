package com.untanglechat.chatapp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatBotTalkResponse {
    
    private Integer status;

    private String reply;

}
