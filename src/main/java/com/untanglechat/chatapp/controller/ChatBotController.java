package com.untanglechat.chatapp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import com.untanglechat.chatapp.dto.HuggingFaceChatModelResponse;
import com.untanglechat.chatapp.dto.request.ChatBotTalkRequest;
import com.untanglechat.chatapp.dto.response.ChatBotTalkResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api")
public class ChatBotController {

    @Value("${HUGGINGFACE.API_TOKEN}")
    private String HUGGINGFACE_API_TOKEN;

    @Value("${HUGGINGFACE.API_URL}")
    private String HUGGINGFACE_API_URL;



    private final WebClient webClient;

    public ChatBotController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostMapping(value="/secured/chatbot/talk")
    public Mono<ChatBotTalkResponse> talkToTheChatBot(@RequestBody Mono<ChatBotTalkRequest> chatBotTalkRequestData) {
        

        System.out.println(HUGGINGFACE_API_TOKEN+"\n"+HUGGINGFACE_API_URL);

        return webClient.post().uri(this.HUGGINGFACE_API_URL)
            .header("Authorization", "Bearer " + this.HUGGINGFACE_API_TOKEN)
            .body(chatBotTalkRequestData.map(c -> c.getMessage()), String.class)
            .retrieve()
            .bodyToMono(HuggingFaceChatModelResponse.class)
            .map(huggingFaceChatModelResponse -> ChatBotTalkResponse.builder()
                .status(HttpStatus.OK.value())
                .reply(huggingFaceChatModelResponse.getGenerated_text())
                .build());

    }
    
    
}
