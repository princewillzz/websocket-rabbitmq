package com.untanglechat.chatapp.services;

import java.util.Arrays;
import java.util.Collection;

import com.untanglechat.chatapp.exceptions.NoUserFoundException;
import com.untanglechat.chatapp.exceptions.UsernameAlreadyExists;
import com.untanglechat.chatapp.models.Profile;
import com.untanglechat.chatapp.repository.ProfileRepository;
import com.untanglechat.chatapp.security.JwtTokenProvider;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
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
    private final JwtTokenProvider jwtTokenProvider;


    @Override
    public Mono<UserDetails> findByUsername(final String username) {
        log.info("Fetching DB: "+ username);
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

    public Mono<Profile> getProfileByUsername(final String username) {
        return profileRepository.findByUsername(username).switchIfEmpty(Mono.error(() ->  new NoUserFoundException("User Does not exists")));
    }

    public Flux<Profile> getAllProfiles(){

        return profileRepository.findAll();
    }

    public Mono<String> registerProfile(final Profile profile) {
        if(profile.getId() != null) throw new IllegalStateException("Invalid Argument");
        profile.setRoles(Arrays.asList("ROLE_USER"));

        profile.setPassword(passwordEncoder.encode(profile.getPassword()));

        return this.profileRepository.existsByUsername(profile.getUsername())
            .onErrorMap(e -> e)
            .flatMap(usernameExists -> {
                if(usernameExists) throw new UsernameAlreadyExists("Duplicate Username");
                return profileRepository.save(profile)
                    .map(it -> {
                        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(it.getRoles().toString());
                        User principal = new User(it.getUsername(), "", authorities);
                        var authentication = new UsernamePasswordAuthenticationToken(principal, authorities);
                        return jwtTokenProvider.createToken(authentication);
                    });
            });
        
       

        // return profileRepository.save(profile)
        //     .map(it -> {
        //         Collection<? extends GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(it.getRoles().toString());
        //         User principal = new User(it.getUsername(), "", authorities);
        //         var authentication = new UsernamePasswordAuthenticationToken(principal, authorities);
        //         return jwtTokenProvider.createToken(authentication);
        //     });
    }
    
}
