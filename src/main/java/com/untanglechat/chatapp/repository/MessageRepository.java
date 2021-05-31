package com.untanglechat.chatapp.repository;

import com.untanglechat.chatapp.models.MessageInfoModel;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;

public interface MessageRepository extends ReactiveMongoRepository<MessageInfoModel, String> {

    Flux<MessageInfoModel> findAllByRoutingKey(String routingKey);
 
}
