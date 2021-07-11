package com.untanglechat.chatapp.controller;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.untanglechat.chatapp.dto.request.RsaTokenUpdateRequest;
import com.untanglechat.chatapp.dto.response.S3UploadResponse;
import com.untanglechat.chatapp.models.Profile;
import com.untanglechat.chatapp.services.ProfileService;
import com.untanglechat.chatapp.util.UtilityService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    
    private final ProfileService profileService;
    private final UtilityService utilityService;
    

    @GetMapping("/users")
    public Flux<Profile>  getUsers() {
        
        return profileService.getAllProfiles();
    }

    @GetMapping("/secured/users/exists/{username}")
    public Mono<Profile> checkUserExistsAndSendInfo(@PathVariable("username") final String username) {
        return profileService.getProfileByUsername(username);
    }

    @PostMapping("/secured/users/exists")
    public Flux<Profile> checkListOfUserExistsAndSendInfo(@RequestBody final Mono<List<String>> usernames) {
        return profileService.getProfilesByUsernames(usernames);
    }

    @PutMapping("/secured/users/rsa-public-key")
    public Mono<Profile> updateRSAToken(@RequestBody final Mono<RsaTokenUpdateRequest> request, @RequestHeader final HttpHeaders headers) {

        final Claims claims = utilityService.extractAllClaimsFromRequest(headers);
        final String subject = claims.getSubject();
        
        return profileService.updatePublicRSATokenForSubject(subject, request);
    }


    @GetMapping(path="users/profile-photo/{filekey}")
    public Mono<ResponseEntity<Flux<ByteBuffer>>> downloadFile(@PathVariable("filekey") String filekey) {    
        
        return profileService.downloadProfilePicture(filekey)
            .map(response -> {
                String filename = getMetadataItem(response.getSdkResponse(),"filename",filekey);            
                return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, response.getSdkResponse().contentType())
                .header(HttpHeaders.CONTENT_LENGTH, Long.toString(response.getSdkResponse().contentLength()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(response.getFlux());
            });
    }

    
    @PostMapping(value = "/secured/users/profile-photo", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<S3UploadResponse> uploadImage(@RequestHeader HttpHeaders headers,
        @RequestPart("profile_img") final Flux<FilePart> profilePhoto) {
        
        
        return profilePhoto
            .flatMap((part)-> profileService.changeProfilePicture(headers, part))
            .collect(Collectors.toList())
            .map((keys)-> new S3UploadResponse(HttpStatus.CREATED, keys));
    }  


        /**
     * Lookup a metadata key in a case-insensitive way.
     * @param sdkResponse
     * @param key
     * @param defaultValue
     * @return
     */
    private String getMetadataItem(GetObjectResponse sdkResponse, String key, String defaultValue) {
        for( Entry<String, String> entry : sdkResponse.metadata().entrySet()) {
            if ( entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return defaultValue;
    }

   
}

