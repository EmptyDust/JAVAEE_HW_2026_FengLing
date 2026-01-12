package com.student.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private static final String SECRET = "student-system-jwt-secret-key-must-be-at-least-256-bits";
    private static final long EXPIRATION = 24 * 60 * 60 * 1000L;

    private static SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Long userId, String username) {
        return generateToken(userId, username, null, null, null);
    }

    public static String generateToken(Long userId, String username, String userType, Long studentId) {
        return generateToken(userId, username, userType, studentId, null);
    }

    public static String generateToken(Long userId, String username, String userType, Long studentId, Long teacherId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        if (userType != null) {
            claims.put("userType", userType);
        }
        if (studentId != null) {
            claims.put("studentId", studentId);
        }
        if (teacherId != null) {
            claims.put("teacherId", teacherId);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSignKey())
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    public static String getUserType(String token) {
        Claims claims = parseToken(token);
        return claims.get("userType", String.class);
    }

    public static Long getStudentId(String token) {
        Claims claims = parseToken(token);
        Object studentId = claims.get("studentId");
        if (studentId == null) {
            return null;
        }
        if (studentId instanceof Integer) {
            return ((Integer) studentId).longValue();
        }
        return (Long) studentId;
    }

    public static Long getTeacherId(String token) {
        Claims claims = parseToken(token);
        Object teacherId = claims.get("teacherId");
        if (teacherId == null) {
            return null;
        }
        if (teacherId instanceof Integer) {
            return ((Integer) teacherId).longValue();
        }
        return (Long) teacherId;
    }

    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
