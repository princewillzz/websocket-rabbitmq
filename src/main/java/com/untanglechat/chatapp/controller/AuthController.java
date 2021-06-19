package com.untanglechat.chatapp.controller;

import com.untanglechat.chatapp.dto.AuthenticationRequest;
import com.untanglechat.chatapp.dto.response.TokenResponse;
import com.untanglechat.chatapp.models.Profile;
import com.untanglechat.chatapp.security.JwtTokenProvider;
import com.untanglechat.chatapp.services.ProfileService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final JwtTokenProvider tokenProvider;
    private final ReactiveAuthenticationManager authenticationManager;
    private final ProfileService profileService;

    @PostMapping("/authenticate")
    public Mono<ResponseEntity<?>> login( @RequestBody Mono<AuthenticationRequest> authRequest) {
        return authRequest
                .flatMap(login -> authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()))
                        .map(auth -> tokenProvider.createToken(auth))
                )
                .map(jwt -> {
                            HttpHeaders httpHeaders = new HttpHeaders();
                            httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
                            var tokenRequest = TokenResponse.builder()
                                .token(jwt)
                                .build();
                            return new ResponseEntity<>(tokenRequest, httpHeaders, HttpStatus.OK);
                        }
                );
    }


    @PostMapping("/users/register")
    public Mono<ResponseEntity<?>> registerUser(@RequestBody final Mono<Profile> profileRequest) {

        return profileRequest
            .flatMap(profile -> {
                final String password = profile.getPassword();
                return profileService
                    .registerProfile(profile)
                    .flatMap(savedProfile -> {
                        return this.login(Mono.just(new AuthenticationRequest(savedProfile.getUsername(), password)));
                    });
                
            });
    }

}