package com.untanglechat.chatapp.services;

import com.untanglechat.chatapp.configuration.MessageListenerContainerFactory;
import com.untanglechat.chatapp.dto.MessageDTO;
import com.untanglechat.chatapp.models.MessageInfoModel;
import com.untanglechat.chatapp.repository.MessageRepository;
import com.untanglechat.chatapp.util.UtilityService;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MessagingService {

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchange;


    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;

    private final MessageListenerContainerFactory messageListenerContainerFactory;

    private final MessageRepository messageRepository;

    private final UtilityService utilityService;

    @Autowired
    protected MessagingService(
        final RabbitAdmin rabbitAdmin,
        final RabbitTemplate rabbitTemplate,
        final MessageRepository messageRepository,
        final UtilityService utilityService,
        final MessageListenerContainerFactory messageListenerContainerFactory
    ) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
        this.messageRepository = messageRepository;
        this.utilityService = utilityService;
        this.messageListenerContainerFactory = messageListenerContainerFactory;
    }

    /**
     * Create a new messaging queue
     * @param queue
     */
    public void createQueue(final Queue queue) {
        System.out.println("Created queue: => " + queue);
        rabbitAdmin.declareQueue(queue);
    }

    
    /**
     * Bind the queue with the routing key and exhange to make it ready to receive messages
     * @param queue
     * @param exchange
     * @param ROUTING_KEY
     * @return
     */
    public Binding bindingQueueWithRoutingKey(Queue queue, TopicExchange exchange, String ROUTING_KEY) {
        System.out.println("Binding queue with key: => " + ROUTING_KEY);
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(ROUTING_KEY);
    }



    /**
     * delete the queue from the message broker
     * @param queue
     */
    public void deleteQueue(final Queue queue) {
        System.out.println("Deleted queue: => " + queue);
        rabbitAdmin.deleteQueue(queue.getName());
    }

    /**
     * Publish data into the message queue
     * @param message
     * @param exchange
     * @param ROUTING_KEY
     * @param queueName
     * @return message
     */
    public Mono<?> sendMessage(
        final MessageDTO message,
        final String exchange,
        final String ROUTING_KEY,
        final String queueName
    ) {
        var queueInfo = rabbitAdmin.getQueueInfo(queueName);
        if(queueInfo != null) {
            rabbitTemplate.convertAndSend(exchange, ROUTING_KEY, message);
        } else {
            this.persistDataToDB(message, ROUTING_KEY);
        }        

        System.out.println("sending message: => " + message + " with key: " + ROUTING_KEY +" with exchange : " + exchange);
        
        return Mono.just(message);
    }


    /**
     * persist message to the database with the Routing key
     * @param message
     * @param routingKey
     */
    private void persistDataToDB(final MessageDTO message, final String routingKey) {
        
        // Create an object of MessageInfoModel and populate it with required info
        final MessageInfoModel messageInfoModel = this.utilityService.messageDtoToMessageInfoModel(message);
        messageInfoModel.setRoutingKey(routingKey);

        messageRepository.save(messageInfoModel).subscribe();
    }

    /**
     * Retrieve all the messageInfoModel from the database whose routingKey matches
     * @param routingKey
     * @return 
     */
    public Flux<MessageInfoModel> getAllMessageWithRoutingKey(final String routingKey) {

        return messageRepository.findAllByRoutingKey(routingKey);
    }


    // Listen for all the messages in the queue
    public Flux<String> incomingMessageFlow(final String queueName) {
        final MessageListenerContainer messageListenerContainer = messageListenerContainerFactory.createMessageListenerContainer(queueName);

        Flux<String> f = Flux.<String> create(emitter -> {
            messageListenerContainer.setupMessageListener((MessageListener) m -> {
                String payload = new String(m.getBody());
                System.out.println(payload);
                emitter.next(payload);
            });
            emitter.onRequest(v -> {
                messageListenerContainer.start();	
            });
            emitter.onDispose(() -> {
                messageListenerContainer.stop();
            });
        });

        return f;
    }
    
}
