package com.untanglechat.chatapp.dto.response;

import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class S3UploadResponse {
    HttpStatus status;
    String[] keys;

    public S3UploadResponse() {}
    
    public S3UploadResponse(HttpStatus status, List<String> keys) {
        this.status = status;
        this.keys = keys == null ? new String[] {}: keys.toArray(new String[] {});

    }
}
