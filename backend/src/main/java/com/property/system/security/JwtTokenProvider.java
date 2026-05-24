package com.property.system.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            log.warn("JWT密钥长度不足32字节，建议使用更长的密钥以提高安全性");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Long userId, Long tenantId, String role, boolean isSuperAdmin) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("tenantId", tenantId)
                .claim("role", role)
                .claim("isSuperAdmin", isSuperAdmin)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long userId, Long tenantId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("tenantId", tenantId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token已过期: {}", e.getMessage());
        } catch (SignatureException e) {
            log.debug("Token签名无效: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("Token格式错误: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.debug("不支持的Token类型: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("Token参数无效: {}", e.getMessage());
        }
        return false;
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean getIsSuperAdmin(String token) {
        Claims claims = parseClaims(token);
        Boolean val = claims.get("isSuperAdmin", Boolean.class);
        return val != null && val;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String userId = claims.getSubject();
        String role = claims.get("role", String.class);

        if (role == null || role.isEmpty()) {
            role = "USER";
        }

        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public Long getTenantId(String token) {
        Claims claims = parseClaims(token);
        return claims.get("tenantId", Long.class);
    }

    public String getRole(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    public Date getExpiration(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration();
    }

    public long getExpirationMs() {
        return this.expiration;
    }

    public boolean isTokenAboutToExpire(String token) {
        try {
            Date expiration = getExpiration(token);
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            return remainingTime < (this.expiration * 0.2);
        } catch (Exception e) {
            return true;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
