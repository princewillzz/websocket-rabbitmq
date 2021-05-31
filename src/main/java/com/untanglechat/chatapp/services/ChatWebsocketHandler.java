package com.untanglechat.chatapp.services;

import java.util.HashMap;
import java.util.Map;

import com.untanglechat.chatapp.configuration.MessageListenerContainerFactory;
import com.untanglechat.chatapp.models.MessageModel;
import com.untanglechat.chatapp.repository.MessageRepository;

import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ChatWebsocketHandler implements WebSocketHandler{

    @Value("${spring.rabbitmq.template.exchange}")
    String exchange;

    @Autowired
    private MessageRepository mRepo;

    @Autowired
    private MessagingService messagingService;


    @Autowired
    private MessageListenerContainerFactory messageListenerContainerFactory;






    @Override
    public Mono<Void> handle(WebSocketSession session) {
      
        System.err.println("session id: ==> "+session.getId());
        
        // Create queue
        // final String queueName = "q3";
        final String queueName = "q"+Math.round(Math.random()*20);//session.getId();
        final String ROUTING_KEY = queueName;

        Queue queue = new Queue(queueName, false);
        messagingService.createQueue(queue);
        messagingService.binding(queue, new TopicExchange(this.exchange), ROUTING_KEY);



        final MessageListenerContainer mlc = messageListenerContainerFactory.createMessageListenerContainer(queueName);

        Flux<String> f = Flux.<String> create(emitter -> {
            mlc.setupMessageListener((MessageListener) m -> {
                String payload = new String(m.getBody());
                System.out.println(payload);
                emitter.next(payload);
            });
            emitter.onRequest(v -> {
                mlc.start();	
            });
            emitter.onDispose(() -> {
                mlc.stop();
            });
        });


        return session.send(
            f.map(msg -> session.textMessage(msg))
        ).and(session.receive()
            .map(msg -> {
                MessageModel m = new MessageModel();
                m.setMessage(msg.getPayloadAsText());
                return m;
            }).flatMap(message -> {

                return messagingService.sendMessage(message, this.exchange, ROUTING_KEY);
            })
        ).doFinally(signalType -> {
            messagingService.deleteQueue(queue);
        }).log();

    }
}