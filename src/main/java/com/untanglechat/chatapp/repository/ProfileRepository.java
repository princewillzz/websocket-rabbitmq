package com.untanglechat.chatapp.repository;

import java.util.List;

import com.untanglechat.chatapp.models.Profile;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProfileRepository extends ReactiveMongoRepository<Profile, String> {

    Mono<Profile> findByUsername(String username);

    Mono<Boolean> existsByUsername(String username);

    Flux<Profile> findAllByUsernameIn(Mono<List<String>> usernames);
    
}
