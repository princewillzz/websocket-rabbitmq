package com.untanglechat.chatapp.services.sms;

import java.util.concurrent.CompletableFuture;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import com.untanglechat.chatapp.configuration.TwilioConfiguration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Qualifier("twilioSMSService")
@Slf4j
@RequiredArgsConstructor
public class TwilioSMSService implements SmsService {

    private final TwilioConfiguration twilioConfiguration;

    @Override
    public Mono<Void> sendSMS(final Mono<SMSRequest> smsRequest) {

        return smsRequest
            .doOnNext(sms -> {
                if(!isPhoneNumberValid(sms.getPhoneNumber())) throw new IllegalArgumentException("Invalid Number");
            })
            .doOnNext(sms -> {
                final MessageCreator creator = Message.creator(
                    new PhoneNumber(sms.getPhoneNumber()),
                    new PhoneNumber(this.twilioConfiguration.getTrialNumber()),
                    sms.getMessage()
                );

                CompletableFuture<Message> futureMessage = creator.createAsync();
                
                Mono
                    .fromFuture(futureMessage)
                    .doOnNext(msg -> {
                        log.info("SMS send Successfully");
                    });

            })
            .then();

        // if(!isPhoneNumberValid(smsRequest.getPhoneNumber())) throw new IllegalArgumentException("Invalid Number");

        // final MessageCreator creator = Message.creator(
        //     new PhoneNumber(smsRequest.getPhoneNumber()),
        //     new PhoneNumber(this.twilioConfiguration.getTrialNumber()),
        //     smsRequest.getMessage()
        // );

        // creator.create();
        
    }

    private boolean isPhoneNumberValid(final String number) {
        // TODO later
        return true;
    }
    
}
