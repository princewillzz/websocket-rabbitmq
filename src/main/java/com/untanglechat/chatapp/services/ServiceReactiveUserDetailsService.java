package com.untanglechat.chatapp.services;

import com.untanglechat.chatapp.repository.ProfileRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class ServiceReactiveUserDetailsService implements ReactiveUserDetailsService{

    @Autowired
    ProfileRepository profileRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        System.err.println("Fetching DB: "+ username);
        return profileRepository.findByUsername(username)
                .map(u -> User
                    .withUsername(u.getUsername()).password(u.getPassword())
                    .authorities(u.getRoles().toArray(new String[0]))
                    .accountExpired(!u.isActive())
                    .credentialsExpired(!u.isActive())
                    .disabled(!u.isActive())
                    .accountLocked(!u.isActive())
                    .build()
                );
    }

    
}
