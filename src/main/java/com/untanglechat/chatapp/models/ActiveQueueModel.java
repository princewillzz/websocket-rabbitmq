package com.untanglechat.chatapp.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Document("active_queue")
public class ActiveQueueModel {
    
    @Id
    private String id;

    private Instant createdAt;

    private String routingKey;

}
