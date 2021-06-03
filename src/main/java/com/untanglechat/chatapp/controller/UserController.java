package com.untanglechat.chatapp.controller;

import com.untanglechat.chatapp.models.Profile;
import com.untanglechat.chatapp.services.ProfileService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    
    private final ProfileService profileService;
    

    @GetMapping("/users")
    public Flux<Profile>  getUsers() {
        
        return profileService.getAllProfiles();
    }

    
    @PostMapping("/users/register")
    public void registerUser(@RequestBody final Profile profile) {

        profileService.registerProfile(profile);
    }
    


}
