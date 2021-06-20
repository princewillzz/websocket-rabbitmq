package com.untanglechat.chatapp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RsaTokenUpdateRequest {
    

    @JsonProperty("rsa_public_key")
    private String rsaPublicKey;

}
