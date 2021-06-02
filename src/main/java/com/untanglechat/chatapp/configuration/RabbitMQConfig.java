package com.untanglechat.chatapp.configuration;


import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
    

    // @Bean
    // public Queue queue() {
    //     return new Queue("queueName");
    // }

    // @Bean
    // public TopicExchange exchange() {
    //     return new TopicExchange("exchange");
    // }


    // @Bean
    // public Binding binding(Queue queue, TopicExchange exchange, String ROUTING_KEY) {
    //     return BindingBuilder
    //             .bind(queue)
    //             .to(exchange)
    //             .with(ROUTING_KEY);
    // }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        template.setMessageConverter(messageConverter());

        return template;
    }

    @Bean
    public RabbitAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

}
