package org.dreamdev.services;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateVoteToken(String voteId) {
        long fiveMinutes = 5 * 60 * 1000L;
        return Jwts.builder()
                .subject(voteId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + fiveMinutes))
                .signWith(key)
                .compact();
    }

    public String extractVoteId(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (JwtException ex) {
            return false;
        }
    }
}