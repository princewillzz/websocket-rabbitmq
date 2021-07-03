package com.untanglechat.chatapp.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@ConfigurationProperties("twilio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwilioConfiguration {

    private String accountSid;

    private String authToken;

    private String trialNumber;
    


}
