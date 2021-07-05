package com.untanglechat.chatapp.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {

    private String id;

    @NotNull
    @NotBlank
    private String username;


    @NotNull
    @NotBlank
    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;


    @JsonProperty(value = "re_password", access = Access.WRITE_ONLY)
    private String rePassword;

    // @Email
    private String email;

    @NotNull
    @NotBlank
    @JsonProperty("country_code")
    private String countryCode;

    @Builder.Default()
    private boolean active = true;

    @Builder.Default()
    private List<String> roles = new ArrayList<>();


    @NotNull
    @NotBlank
    private String publicRSAKey;

    @JsonProperty("profile_picture")
    private String profilePictureS3ObjectId;

    private String verificationOTP;
    
}
