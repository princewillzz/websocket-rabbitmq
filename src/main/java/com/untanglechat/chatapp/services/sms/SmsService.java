package com.untanglechat.chatapp.services.sms;

import reactor.core.publisher.Mono;

public interface SmsService {

    Mono<Void> sendSMS(Mono<SMSRequest> smsRequest);
    
}
