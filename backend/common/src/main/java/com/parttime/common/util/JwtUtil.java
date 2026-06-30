package com.parttime.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT令牌工具类
 * <p>
 * 使用HS256算法签名，密钥从环境变量JWT_SECRET读取。
 * JWT payload包含：user_id、role、permissions[]、exp。
 * </p>
 * <p>
 * 适用场景：用户认证、权限控制、单点登录。
 * </p>
 */
public class JwtUtil {

    private static volatile String secret;

    /**
     * 获取JWT签名密钥
     * <p>
     * 使用双重检查锁确保线程安全，密钥从环境变量JWT_SECRET读取，
     * 如果环境变量未设置，使用默认密钥（仅用于开发环境）。
     * </p>
     *
     * @return JWT签名密钥
     */
    private static String getSecret() {
        if (secret == null) {
            synchronized (JwtUtil.class) {
                if (secret == null) {
                    secret = System.getenv("JWT_SECRET");
                    if (secret == null || secret.isEmpty()) {
                        secret = "default_jwt_secret_key_for_development";
                    }
                }
            }
        }
        return secret;
    }

    /**
     * 生成JWT令牌（默认有效期7天）
     *
     * @param userId      用户ID
     * @param role        用户角色（student/enterprise/admin）
     * @param permissions 用户权限列表
     * @return JWT令牌字符串
     */
    public static String generateToken(String userId, String role, List<String> permissions) {
        return generateToken(userId, role, permissions, 604800);
    }

    /**
     * 生成JWT令牌（自定义有效期）
     *
     * @param userId        用户ID
     * @param role          用户角色（student/enterprise/admin）
     * @param permissions   用户权限列表
     * @param expireSeconds 过期时间（秒），默认604800秒（7天）
     * @return JWT令牌字符串
     */
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
                .signWith(SignatureAlgorithm.HS256, getSecret())
                .compact();
    }

    /**
     * 解析JWT令牌
     *
     * @param token JWT令牌
     * @return Claims对象，包含所有payload信息
     * @throws io.jsonwebtoken.JwtException 令牌无效时抛出
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSecret())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证JWT令牌有效性
     * <p>
     * 验证签名和过期时间，不抛出异常。
     * </p>
     *
     * @param token JWT令牌
     * @return true表示令牌有效，false表示无效或已过期
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从JWT令牌中提取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     * @throws io.jsonwebtoken.JwtException 令牌无效时抛出
     */
    public static String getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("user_id", String.class);
    }

    /**
     * 从JWT令牌中提取用户角色
     *
     * @param token JWT令牌
     * @return 用户角色
     * @throws io.jsonwebtoken.JwtException 令牌无效时抛出
     */
    public static String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 从JWT令牌中提取用户权限列表
     *
     * @param token JWT令牌
     * @return 权限列表
     * @throws io.jsonwebtoken.JwtException 令牌无效时抛出
     */
    @SuppressWarnings("unchecked")
    public static List<String> getPermissions(String token) {
        Claims claims = parseToken(token);
        return claims.get("permissions", List.class);
    }
}