package com.untanglechat.chatapp.repository;

import com.untanglechat.chatapp.models.ActiveQueueModel;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

public interface ActiveQueueRepository extends ReactiveMongoRepository<ActiveQueueModel, String> {
    
    Mono<ActiveQueueModel> findByRoutingKey(String routingKey);

    Mono<Void> deleteByRoutingKey(String routingKey);

    Mono<Boolean> existsByRoutingKey(String routingKey);

}
