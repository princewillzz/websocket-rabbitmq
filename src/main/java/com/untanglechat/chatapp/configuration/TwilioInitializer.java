package com.untanglechat.chatapp.configuration;

import com.twilio.Twilio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class TwilioInitializer {
    
    private final TwilioConfiguration twilioConfiguration;


    @Autowired
    public TwilioInitializer(final TwilioConfiguration twilioConfiguration) {
        this.twilioConfiguration = twilioConfiguration;

        Twilio.init(twilioConfiguration.getAccountSid(), twilioConfiguration.getAuthToken());

        log.info("TWILIO account initialized");
    }

}
