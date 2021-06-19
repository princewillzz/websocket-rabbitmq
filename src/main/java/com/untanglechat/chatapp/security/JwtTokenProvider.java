package com.untanglechat.chatapp.security;
import static java.util.stream.Collectors.joining;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;

import com.untanglechat.chatapp.models.Profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class JwtTokenProvider {
    private static final String AUTHORITIES_KEY = "roles";
    // private static final String PUBLIC_RSA_KEY = "public_rsa_key";

    @Autowired
    JwtProperties jwtProperties;

    private SecretKey secretKey;

    @PostConstruct
    protected void init() {
        var secret = Base64.getEncoder().encodeToString(jwtProperties.getSecretKey().getBytes());
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Authentication authentication) {

        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Claims claims = Jwts.claims().setSubject(username);
        if (!authorities.isEmpty()) {
            claims.put(AUTHORITIES_KEY, authorities.stream().map(GrantedAuthority::getAuthority).collect(joining(",")));
        }
        
        // final UserPrincipal userPrincipal = (UserPrincipal)authentication.getPrincipal();
        // claims.put(PUBLIC_RSA_KEY, userPrincipal.getPublicRSAKey());
        

        Date now = new Date();
        Date validity = new Date(now.getTime() + this.jwtProperties.getValidityInMs());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(this.secretKey, SignatureAlgorithm.HS256)
                .compact();

    }


    public Claims extractAllClaims(String token) {
        // System.out.println("Extracting claims: " + this.secretKey + " " + token);
        return Jwts.parserBuilder().setSigningKey(this.secretKey)
            .build().parseClaimsJws(token).getBody();
    }
    

    public Authentication getAuthentication(String token) {
        Claims claims = this.extractAllClaims(token);
        // Collection<? extends GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(claims.get(AUTHORITIES_KEY).toString());
        // User principal = new User(claims.getSubject(), "", authorities);

        
        // System.err.println(Arrays(claims.get(AUTHORITIES_KEY).toString().split(",")));
        // System.out.println("Sub: "+claims.getSubject());
        // System.out.println("Auth"+claims.get(AUTHORITIES_KEY));
        // System.out.println("Auth list"+claims.get(AUTHORITIES_KEY).toString().split(","));
        final UserPrincipal userPrincipal = new UserPrincipal(
            Profile.builder()
            .username(claims.getSubject())
            .roles(Arrays.asList(claims.get(AUTHORITIES_KEY).toString().split(",")))
            .build());

            
       
        return new UsernamePasswordAuthenticationToken(userPrincipal, token, userPrincipal.getAuthorities());
    }
    
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build()
                    .parseClaimsJws(token);
            if (claims.getBody().getExpiration().before(new Date())) {
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace.", e);
        }
        return false;
    }
}