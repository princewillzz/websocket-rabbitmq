package com.untanglechat.chatapp.models;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
// @Document(collection = "message")
public class MessageModel {

    // @Id
    private String id;
    private String message;
    private String type;

    private Date sentTime = new Date();
    private String sentBy;
    private String sentTo;

    
}
