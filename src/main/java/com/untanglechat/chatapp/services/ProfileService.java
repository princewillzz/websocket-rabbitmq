package com.untanglechat.chatapp.services;

import java.util.Arrays;

import com.untanglechat.chatapp.dto.request.RsaTokenUpdateRequest;
import com.untanglechat.chatapp.exceptions.NoUserFoundException;
import com.untanglechat.chatapp.exceptions.UsernameAlreadyExists;
import com.untanglechat.chatapp.models.Profile;
import com.untanglechat.chatapp.repository.ProfileRepository;
import com.untanglechat.chatapp.security.UserPrincipal;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService implements ReactiveUserDetailsService{

    private final ProfileRepository profileRepository;
    
    private final PasswordEncoder passwordEncoder;

  

    @Override
    public Mono<UserDetails> findByUsername(final String username) {
        log.info("Fetching DB: "+ username);
        return profileRepository.findByUsername(username)
                    .map(profile -> new UserPrincipal(profile));
    }

    public Mono<Profile> getProfileByUsername(final String username) throws NoUserFoundException {
        return profileRepository.findByUsername(username).switchIfEmpty(Mono.error(() ->  new NoUserFoundException("User Does not exists")));
    }

    public Flux<Profile> getAllProfiles(){

        return profileRepository.findAll();
    }

    public Mono<Profile> registerProfile(final Profile profile) throws UsernameAlreadyExists {
        if(profile.getId() != null) throw new IllegalStateException("Invalid Argument");
        profile.setRoles(Arrays.asList("ROLE_USER"));

        profile.setPassword(passwordEncoder.encode(profile.getPassword()));

        return this.profileRepository.existsByUsername(profile.getUsername())
            .onErrorMap(e -> e)
            .flatMap(usernameExists -> {
                if(usernameExists) throw new UsernameAlreadyExists("Duplicate Username");
                return profileRepository.save(profile);
            });
        
    }
    

    public Mono<Profile> updatePublicRSATokenForSubject(final String subject, final RsaTokenUpdateRequest tokenUpdateRequest) {
        return this.getProfileByUsername(subject)
            .flatMap(profile -> {
                profile.setPublicRSAKey(tokenUpdateRequest.getRsaPublicKey());
                return this.profileRepository.save(profile);                
            });
    }

}
