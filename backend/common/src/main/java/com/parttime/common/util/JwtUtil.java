package com.parttime.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtUtil {

    private static volatile Key secretKey;

    private static Key getSecretKey() {
        if (secretKey == null) {
            synchronized (JwtUtil.class) {
                if (secretKey == null) {
                    String secret = System.getenv("JWT_SECRET");
                    if (secret == null || secret.isEmpty()) {
                        secret = System.getProperty("jwt.secret");
                    }
                    if (secret == null || secret.isEmpty()) {
                        secret = "default_jwt_secret_key_for_development";
                    }
                    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
                    if (keyBytes.length < 32) {
                        byte[] padded = new byte[32];
                        System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
                        keyBytes = padded;
                    }
                    secretKey = Keys.hmacShaKeyFor(keyBytes);
                }
            }
        }
        return secretKey;
    }

    public static String generateToken(String userId, String role, List<String> permissions) {
        return generateToken(userId, role, permissions, 604800);
    }

    public static String generateToken(String userId, String role, List<String> permissions, long expireSeconds) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("role", role);
        claims.put("permissions", permissions);

        Date now = new Date();
        Date expire = new Date(now.getTime() + expireSeconds * 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expire)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public static String getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("user_id", String.class);
    }

    public static String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getPermissions(String token) {
        Claims claims = parseToken(token);
        return claims.get("permissions", List.class);
    }
}