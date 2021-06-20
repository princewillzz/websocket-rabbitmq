package com.untanglechat.chatapp.controller;

import com.untanglechat.chatapp.dto.request.RsaTokenUpdateRequest;
import com.untanglechat.chatapp.models.Profile;
import com.untanglechat.chatapp.security.JwtTokenProvider;
import com.untanglechat.chatapp.services.ProfileService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    
    private final ProfileService profileService;
    private final JwtTokenProvider jwtTokenProvider;
    

    @GetMapping("/users")
    public Flux<Profile>  getUsers() {
        
        return profileService.getAllProfiles();
    }

    @GetMapping("/secured/users/exists/{username}")
    public Mono<Profile> checkUserExistsAndSendInfo(@PathVariable("username") final String username) {
        return profileService.getProfileByUsername(username);
    }

    @PutMapping("/secured/users/rsa-public-key")
    public Object updateRSAToken(@RequestBody final RsaTokenUpdateRequest request, ServerWebExchange exchange) {

        final String bearer = exchange.getRequest().getHeaders().get("Authorization").get(0);
        final String token = bearer.replace("Bearer ", "");

        final Claims claims = jwtTokenProvider.extractAllClaims(token);
        final String subject = claims.getSubject();
        
        return profileService.updatePublicRSATokenForSubject(subject, request);
    }


}

