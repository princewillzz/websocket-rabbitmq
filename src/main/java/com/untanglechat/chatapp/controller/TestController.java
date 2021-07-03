package com.untanglechat.chatapp.controller;

import com.untanglechat.chatapp.dto.MessageDTO;
import com.untanglechat.chatapp.repository.MessageRepository;
import com.untanglechat.chatapp.services.MessagingService;
import com.untanglechat.chatapp.services.sms.SMSRequest;
import com.untanglechat.chatapp.services.sms.SmsService;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class TestController {


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    MessagingService messagingService;

    @Value("${spring.rabbitmq.template.exchange}")
    String exchange;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    @Qualifier("AmazonSMSService")
    private SmsService smsService;

    @PostMapping("/sms")
    public Mono<Void> sendSMS(@RequestBody Mono<SMSRequest> smsReq) {
        System.err.println(smsReq);
        return smsService.sendSMS(smsReq);
    }
    


    @PostMapping(value="/publish/{queue}")
    public Object postMethodName(@PathVariable("queue")String queueName, @RequestBody String entity) {
        
        MessageDTO message = new MessageDTO();
        message.setMessage(entity);
        message.setSentBy("9832145254");
        message.setType("text");
        message.setSentTo(queueName);


        var queueInfo = rabbitAdmin.getQueueInfo(queueName);
        String ROUTING_KEY = queueName;


        // if(queueInfo != null) {
            
        //     System.err.println("Queue present");
        //     System.out.println(queueInfo);

        // } else {
        //     System.err.println("No queue");
        //     messagingService.createBindAndInitializeQueue(queueName, new TopicExchange(this.exchange), ROUTING_KEY);
        //     // rabbitAdmin.declareQueue(queue);

        //     // rabbitAdmin.declareBinding(messagingService.binding(queue, new TopicExchange("exchange_secret"), ROUTING_KEY));
            
        // }
        

        System.out.println("OUR exhange: "+ this.exchange);
        messagingService.sendMessage(message, this.exchange, ROUTING_KEY, queueName);

        
        return message;
    }
    



    @GetMapping(value = "/messages")
    public Object getMethodName() {

        System.out.println("exchange" + this.exchange);

        // MessageInfoModel msg = new MessageInfoModel();
        // msg.setMessage("message");
        // msg.setRoutingKey("123");

        // messageRepository.save(msg).subscribe();
        // messageRepository.findAllByRoutingKey("123").subscribe(item -> {
        //     System.out.println(item);
        // });

        messagingService.getAllMessageWithRoutingKey("123").subscribe(it -> {
            System.out.println(it);
        });
        

        return "{done}";
    }

    @GetMapping("/secured/auth/authenticate")
    public String securedHello() {return "hello";}

    @GetMapping(value="/test/{id}")
    public Object sdatst(@PathVariable String id) {
       
        return messageRepository.existsByRoutingKey(id);
    }
    

}
