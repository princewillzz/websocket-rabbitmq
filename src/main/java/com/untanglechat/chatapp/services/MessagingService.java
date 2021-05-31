package com.untanglechat.chatapp.services;

import com.untanglechat.chatapp.models.MessageModel;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class MessagingService {

    @Value("${spring.rabbitmq.template.exchange}")
    String exchange;


    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    
    public Binding binding(Queue queue, TopicExchange exchange, String ROUTING_KEY) {
        System.out.println("Binding queue with key: => " + ROUTING_KEY);
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(ROUTING_KEY);
    }

    public void createQueue(final Queue queue) {
        System.out.println("Created queue: => " + queue);
        rabbitAdmin.declareQueue(queue);
    }

    public void deleteQueue(final Queue queue) {
        System.out.println("Deleted queue: => " + queue);
        rabbitAdmin.deleteQueue(queue.getName());
    }

    public Mono<?> sendMessage(
        final MessageModel message,
        final String exchange,
        final String ROUTING_KEY
    ) {
        
        rabbitTemplate.convertAndSend(exchange, ROUTING_KEY, message);
        
        
        System.out.println("sending message: => " + message + " with key: " + ROUTING_KEY +" with exchange : " + exchange);
        
        return Mono.just(message);
    }
    
}
