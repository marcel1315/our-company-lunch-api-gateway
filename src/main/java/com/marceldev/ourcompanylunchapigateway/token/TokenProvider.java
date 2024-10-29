package com.marceldev.ourcompanylunchapigateway.token;

import io.jsonwebtoken.Claims;
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
import reactor.core.publisher.Mono;

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

  public Mono<String> getUsername(String token) {
    return parseClaimsCache(token)
        .map(Claims::getSubject);
  }

  public Mono<String> getRole(String token) {
    return parseClaimsCache(token)
        .map(claims -> claims.get(KEY_ROLE, String.class));
  }

  private Mono<Claims> parseClaimsCache(String token) {
    if (!StringUtils.hasText(token)) {
      return Mono.error(IllegalArgumentException::new);
    }

    return Mono.fromCallable(() ->
            Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
        )
        .cache();
  }

  public Mono<Authentication> getAuthentication(String token) {
    return Mono.zip(getUsername(token), getRole(token))
        .map(tuple -> {
          String username = tuple.getT1();
          GrantedAuthority authority = new SimpleGrantedAuthority(tuple.getT2());
          return new UsernamePasswordAuthenticationToken(
              username,
              null,
              List.of(authority)
          );
        });
  }

  private SecretKey getSecretKey() {
    byte[] keyBytes = this.secretKey.getBytes(StandardCharsets.UTF_8);
    return new SecretKeySpec(keyBytes, "HmacSHA512");
  }

  private long getExpiredInSecond() {
    return 1000 * 60 * 60 * expiredInHour;
  }
}