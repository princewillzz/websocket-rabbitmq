package com.untanglechat.chatapp.services.sms;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@Qualifier("AmazonSMSService")
public class AWSSNSClient implements SmsService {
    
    @Value("${amazon.sns.access-key}")
    private String ACCESS_KEY;

    @Value("${amazon.sns.secret-key}")
    private String SECRET_KEY;

    private static String AWS_SNS_SENDERID = "AWS.SNS.SMS.SenderID";
    private static String AWS_SNS_SMSTYPE = "AWS.SNS.SMS.SMSType";

    public static String AWS_ACCESS_KEY_ID = "aws.accessKeyId";
    public static String AWS_SECRET_KEY = "aws.secretKey";

    @PostConstruct
    void postConstruct() {
        System.setProperty(AWS_ACCESS_KEY_ID, this.ACCESS_KEY);
        System.setProperty(AWS_SECRET_KEY, this.SECRET_KEY);
    }

    @Override
    public Mono<Void> sendSMS(Mono<SMSRequest> smsRequest) {
        AmazonSNSAsync snsClient = AmazonSNSAsyncClient
            .asyncBuilder()
            .withRegion(Regions.AP_SOUTH_1)
            .build();

        Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
        smsAttributes.put(
            AWSSNSClient.AWS_SNS_SENDERID,
            new MessageAttributeValue()
                .withStringValue("CHATAPP")
                .withDataType("String")
        );

        smsAttributes.put(
            AWSSNSClient.AWS_SNS_SMSTYPE,
            new MessageAttributeValue()
                .withStringValue("Transactional")
                .withDataType("String")
        );

        return smsRequest
            .doOnNext(sms -> {
                final PublishRequest publishRequest = new PublishRequest()
                        .withMessage(sms.getMessage())
                        .withPhoneNumber(sms.getPhoneNumber())
                        .withMessageAttributes(smsAttributes);
                snsClient.publishAsync(publishRequest, new AsyncHandler<PublishRequest,PublishResult>(){

                    @Override
                    public void onError(Exception exception) {
                        log.error(exception.getMessage());
                    }

                    @Override
                    public void onSuccess(PublishRequest request, PublishResult result) {
                        log.info("Message Successfully sent!! to "+ sms.getPhoneNumber() + " ==> " + result.getMessageId());  
                        System.out.println(result);  
                        System.out.println(request);                 
                    }
                    
                });
                    
            }).then();   

    }

}
