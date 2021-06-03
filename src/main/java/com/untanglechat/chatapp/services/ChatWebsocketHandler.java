package com.untanglechat.chatapp.services;

import javax.annotation.PostConstruct;
import javax.security.auth.Subject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.untanglechat.chatapp.dto.MessageDTO;
import com.untanglechat.chatapp.security.JwtTokenProvider;
import com.untanglechat.chatapp.util.UtilityService;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ChatWebsocketHandler implements WebSocketHandler{

    @Value("${spring.rabbitmq.template.exchange}")
    String exchange;


    private final UtilityService utilityService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MessagingService messagingService;

    private ObjectMapper objectMapper;

    @PostConstruct
    private void postConstruct() {
        this.objectMapper = new ObjectMapper();
    }


    @Override
    public Mono<Void> handle(WebSocketSession session) {
      
        System.err.println("session id: ==> " + session.getId());
        
        // Get user info from the JWT
        final String token = utilityService.getTokenFromHandshakeInfo(session.getHandshakeInfo());
        var claimsFromJWT = jwtTokenProvider.extractAllClaims(token);

        final String meSubject = claimsFromJWT.getSubject();


        // Create queue
        final String queueName = meSubject;
        final String ROUTING_KEY = queueName;
        final TopicExchange exchange = new TopicExchange(this.exchange);

        final Queue queue = messagingService.createBindAndInitializeQueue(queueName, exchange, ROUTING_KEY);


        return session
            .send(
                // Stream data from its concerned queue through web socket
                messagingService.incomingMessageFlow(queueName).map(session::textMessage)
            )
            .and(
                // TODO :- send it to the queue instead of the client 
                // send all the data from the database to the queue 

                // Currently sending to the channel
                session.send(messagingService.getAllMessageWithRoutingKey(ROUTING_KEY)
                    .map(msg -> {
                        try {
                            return session.textMessage(objectMapper.writeValueAsString(msg));
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            return null;
                        }
                        
                    }).filter(msg -> msg != null)
                )
            )
            .and(
                session.receive()
                .map(msg -> {

                    // After the frontend sends the whole object
                    // System.out.println("Payload received: " + msg.getPayloadAsText());
                    // try {
                    //     var o =  objectMapper.readValue(msg.getPayloadAsText(), MessageDTO.class);
                    //     System.err.println("payload to obj: ===> " + o);
                    // } catch (Exception e) {
                    //     //TODO: handle exception
                    //     System.out.println(e.getMessage());
                    // }

                    MessageDTO m = new MessageDTO();
                    m.setMessage(msg.getPayloadAsText());
                    return m;
                }).flatMap(message -> {
                    return messagingService.sendMessage(message, this.exchange, ROUTING_KEY, queueName);
                })
            ).doFinally(signalType -> {
                
                // Delete my queue
                messagingService.deleteQueue(queue);

            }).log();

    }

}