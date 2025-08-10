package com.example.bankcards.security.parser;

import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.function.Function;

public interface JwtParser {
    Claims parseToken(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    String extractUsername(String token);
    Date extractExpiration(String token);
    boolean isTokenExpired(String token);
}
