package com.marceldev.ourcompanylunchapigateway.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

public abstract class TokenResolveUtil {

  private static final String TOKEN_HEADER = "Authorization";
  private static final String TOKEN_PREFIX = "Bearer ";

  public static Mono<String> resolve(ServerHttpRequest request) {
    String bearerToken = request.getHeaders().getFirst(TOKEN_HEADER);
    if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
      String token = bearerToken.substring(TOKEN_PREFIX.length());
      return Mono.just(token);
    }
    return Mono.empty();
  }
}
