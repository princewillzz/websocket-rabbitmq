package com.untanglechat.chatapp.repository;

import com.untanglechat.chatapp.models.RegisterProfileOTP;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisterProfileOTPRepository extends ReactiveMongoRepository<RegisterProfileOTP, String> {
    
}
