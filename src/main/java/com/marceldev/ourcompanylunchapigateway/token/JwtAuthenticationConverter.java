package com.marceldev.ourcompanylunchapigateway.token;

import com.marceldev.ourcompanylunchapigateway.util.TokenResolveUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class JwtAuthenticationConverter implements ServerAuthenticationConverter {

  @Override
  public Mono<Authentication> convert(ServerWebExchange exchange) {
    String token = TokenResolveUtil.resolve(exchange.getRequest());

    if (token != null) {
      return Mono.just(new UsernamePasswordAuthenticationToken("", token));
    }
    return Mono.empty();
  }
}
