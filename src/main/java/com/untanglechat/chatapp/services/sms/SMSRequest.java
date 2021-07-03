package com.untanglechat.chatapp.services.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SMSRequest {

    private String phoneNumber;

    private String message;

}
