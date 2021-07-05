package com.untanglechat.chatapp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "profile")
public class Profile {

    @Id
    private String id;

    @NotNull
    @NotBlank
    private String username;


    @NotNull
    @NotBlank
    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;


    @NotNull
    @NotBlank
    @JsonProperty("country_code")
    private String countryCode;

    // @Email
    private String email;

    @Builder.Default()
    private boolean active = true;

    @Builder.Default()
    private List<String> roles = new ArrayList<>();


    @NotNull
    @NotBlank
    private String publicRSAKey;

    @JsonProperty("profile_picture")
    private String profilePictureS3ObjectId;
    
}
