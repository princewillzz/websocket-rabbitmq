package com.untanglechat.chatapp.repository;

import com.untanglechat.chatapp.models.Profile;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

public interface ProfileRepository extends ReactiveMongoRepository<Profile, String> {

    Mono<Profile> findByUsername(String username);
    
}
