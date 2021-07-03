package com.untanglechat.chatapp.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {

    private String id;

    private String username;

    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;

    // @Email
    private String email;

    @Builder.Default()
    private boolean active = true;

    @Builder.Default()
    private List<String> roles = new ArrayList<>();

    private String publicRSAKey;

    @JsonProperty("profile_picture")
    private String profilePictureS3ObjectId;

    private String verificationOTP;
    
}
