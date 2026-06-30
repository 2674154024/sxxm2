package com.parttime.common.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    @Test
    public void testGenerateAndParseToken() {
        String userId = "user-123456";
        String role = "student";
        List<String> permissions = Arrays.asList("read", "write");

        String token = JwtUtil.generateToken(userId, role, permissions);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = JwtUtil.parseToken(token);
        assertEquals(userId, claims.get("user_id"));
        assertEquals(role, claims.get("role"));
        assertTrue(JwtUtil.validateToken(token));
    }

    @Test
    public void testGetUserId() {
        String userId = "test-user-id";
        String token = JwtUtil.generateToken(userId, "test-role", Arrays.asList("perm1"));
        assertEquals(userId, JwtUtil.getUserId(token));
    }

    @Test
    public void testGetRole() {
        String role = "enterprise";
        String token = JwtUtil.generateToken("test-user", role, Arrays.asList("perm1"));
        assertEquals(role, JwtUtil.getRole(token));
    }

    @Test
    public void testGetPermissions() {
        List<String> permissions = Arrays.asList("read", "write", "delete");
        String token = JwtUtil.generateToken("test-user", "role", permissions);
        List<String> result = JwtUtil.getPermissions(token);
        assertEquals(permissions.size(), result.size());
        assertTrue(result.containsAll(permissions));
    }

    @Test
    public void testTokenExpiry() {
        String token = JwtUtil.generateToken("test-user", "role", Arrays.asList("perm"), 1);
        assertTrue(JwtUtil.validateToken(token));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertFalse(JwtUtil.validateToken(token));
    }
}