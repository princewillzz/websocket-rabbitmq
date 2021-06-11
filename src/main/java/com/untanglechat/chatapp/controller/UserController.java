package com.untanglechat.chatapp.controller;

import com.untanglechat.chatapp.models.Profile;
import com.untanglechat.chatapp.services.ProfileService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    
    private final ProfileService profileService;
    

    @GetMapping("/users")
    public Flux<Profile>  getUsers() {
        
        return profileService.getAllProfiles();
    }

    @GetMapping("/secured/users/exists/{username}")
    public Mono<Profile> checkUserExistsAndSendInfo(@PathVariable("username") final String username) {
        return profileService.getProfileByUsername(username);
    }
    


}
