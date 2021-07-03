package com.untanglechat.chatapp.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "register_profile_otp")
public class RegisterProfileOTP {

    @Id
    private String phoneNumber;

    private String otp;

    private Instant createdAt;

}
