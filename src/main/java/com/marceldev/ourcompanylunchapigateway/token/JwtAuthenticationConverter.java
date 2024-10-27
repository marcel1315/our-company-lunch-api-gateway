package com.marceldev.ourcompanylunchapigateway.token;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class JwtAuthenticationConverter implements ServerAuthenticationConverter {

  private static final String TOKEN_HEADER = "Authorization";
  private static final String TOKEN_PREFIX = "Bearer ";

  @Override
  public Mono<Authentication> convert(ServerWebExchange exchange) {
    String token = resolveTokenFromRequest(exchange.getRequest());

    if (token != null) {
      return Mono.just(new UsernamePasswordAuthenticationToken("", token));
    }
    return Mono.empty();
  }

  private String resolveTokenFromRequest(ServerHttpRequest request) {
    String token = request.getHeaders().getFirst(TOKEN_HEADER);
    if (token != null && token.startsWith(TOKEN_PREFIX)) {
      return token.substring(TOKEN_PREFIX.length());
    }
    return null;
  }
}
