package com.marceldev.ourcompanylunchapigateway.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class TokenProvider {

  private final long expiredInHour;

  private final String secretKey;

  private static final String KEY_ROLE = "role";

  public String generateToken(String email, String role) {
    Claims claims = Jwts.claims()
        .subject(email)
        .add(KEY_ROLE, role)
        .build();

    Date now = new Date();
    Date expireDate = new Date(now.getTime() + getExpiredInSecond());

    return Jwts.builder()
        .claims(claims)
        .issuedAt(now)
        .expiration(expireDate)
        .signWith(getSecretKey())
        .compact();
  }

  public String getUsername(String token) {
    return this.parseClaims(token).getSubject();
  }

  public String getRole(String token) {
    return this.parseClaims(token).get(KEY_ROLE, String.class);
  }

  public boolean validateToken(String token) {
    if (!StringUtils.hasText(token)) {
      return false;
    }

    Claims claims = this.parseClaims(token);
    return claims.getExpiration().after(new Date());
  }

  private Claims parseClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(getSecretKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }

  private SecretKey getSecretKey() {
    byte[] keyBytes = this.secretKey.getBytes(StandardCharsets.UTF_8);
    return new SecretKeySpec(keyBytes, "HmacSHA512");
  }

  public Authentication getAuthentication(String token) {
    String username = getUsername(token);
    GrantedAuthority authority = new SimpleGrantedAuthority(getRole(token));
    return new UsernamePasswordAuthenticationToken(
        username,
        null,
        List.of(authority)
    );
  }

  private long getExpiredInSecond() {
    return 1000 * 60 * 60 * expiredInHour;
  }
}