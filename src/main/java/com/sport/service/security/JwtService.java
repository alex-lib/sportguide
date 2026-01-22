package com.sport.service.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sport.service.entities.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class JwtService {
    private final String secret;
    private final Long expirationMs;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    @Autowired
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") Long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).build();
    }

    public String generateToken(Subscriber subscriber) {
        long now = System.currentTimeMillis();
        Date expires = new Date(now + expirationMs);

        return JWT.create()
                .withSubject(subscriber.getId().toString())
                .withClaim("username", subscriber.getUsername())
                .withClaim("id", subscriber.getId())
                .withClaim("role", subscriber.getRole().name())
                .withIssuedAt(new Date(now))
                .withExpiresAt(expires)
                .sign(algorithm);
    }

    public DecodedJWT parseAndValidate(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }
}