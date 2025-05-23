package com.projektamtai.projekt.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class Authorize {

    @Value("${jwt.secret}")
    private String secret;

    public boolean isAuthorized(String token, Long requiredPrivileges) {
        try {
            token = token.substring(7); // Remove "Bearer " from the token
            Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();

            Long userPrivileges = claims.get("role", Long.class);

            if (userPrivileges == null) {
                return false;
            }

            return userPrivileges >= requiredPrivileges;

        } catch (Exception e) {
            return false;
        }
    }
}
