package com.marceldev.ourcompanylunchapigateway.util;

import org.springframework.http.server.reactive.ServerHttpRequest;

public abstract class TokenResolveUtil {

  private static final String TOKEN_HEADER = "Authorization";
  private static final String TOKEN_PREFIX = "Bearer ";

  public static String resolve(ServerHttpRequest request) {
    String bearerToken = request.getHeaders().getFirst(TOKEN_HEADER);
    if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
      return bearerToken.substring(TOKEN_PREFIX.length());
    }
    return null;
  }
}
