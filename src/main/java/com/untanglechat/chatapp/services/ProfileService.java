package com.untanglechat.chatapp.services;

import java.util.Arrays;

import com.untanglechat.chatapp.models.Profile;
import com.untanglechat.chatapp.repository.ProfileRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProfileService implements ReactiveUserDetailsService{

    private final ProfileRepository profileRepository;
    
    private final PasswordEncoder passwordEncoder;

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

    public Flux<Profile> getAllProfiles(){

        return profileRepository.findAll();
    }

    public void registerProfile(final Profile profile) {
        if(profile.getId() != null) throw new IllegalStateException("Invalid Argument");
        profile.setRoles(Arrays.asList("ROLE_USER"));
        profile.setPassword(passwordEncoder.encode(profile.getPassword()));

        profileRepository.save(profile).subscribe();
    }
    
}
