package com.yahoo.identity.services.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.yahoo.identity.IdentityError;
import com.yahoo.identity.IdentityException;
import com.yahoo.identity.services.key.KeyService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.annotation.Nonnull;

public class TokenImplVulnerable implements Token {

    private final KeyService keyService;
    private TokenType tokenType;
    private Instant issueTime;
    private Instant expireTime;
    private String subject;

    public TokenImplVulnerable(@Nonnull KeyService keyService) {
        this.keyService = keyService;
    }

    @Override
    @Nonnull
    public String toString() {
        try {
            Algorithm algorithm = Algorithm.HMAC256(this.keyService.getSecret("token.key"));
            return JWT.create()
                .withExpiresAt(Date.from(this.expireTime))
                .withIssuedAt(Date.from(this.issueTime))
                .withSubject(this.subject)
                .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new IdentityException(IdentityError.INVALID_CREDENTIAL, "JWT verification does not succeed.", e);
        }
    }

    @Override
    public void validate() {
        boolean valid = this.expireTime.compareTo(Instant.now()) > 0;
        if (!valid) {
            throw new IdentityException(IdentityError.INVALID_CREDENTIAL, "Token has been expired.");
        }
    }
}