package com.untanglechat.chatapp.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String secretKey = "flzxsqcysyadadadhljtasdadjsadjsadjsakdjkdjsakldjsaljdsadjsajdadjaldja";
    //validity in milliseconds
    private long validityInMs = 1000 * 60 * 60 * 24; // 24h
}