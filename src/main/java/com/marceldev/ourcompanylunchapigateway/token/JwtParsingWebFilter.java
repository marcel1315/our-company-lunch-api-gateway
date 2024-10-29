package com.marceldev.ourcompanylunchapigateway.token;

import com.marceldev.ourcompanylunchapigateway.util.TokenResolveUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtParsingWebFilter implements WebFilter {

  private final TokenProvider tokenProvider;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String token = TokenResolveUtil.resolve(exchange.getRequest());

    return Mono.zip(tokenProvider.getUsername(token), tokenProvider.getRole(token))
        .flatMap(tuple -> {
          String userId = tuple.getT1();
          String role = tuple.getT2();
          ServerHttpRequestDecorator modified = new ServerHttpRequestDecorator(
              exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
              HttpHeaders headers = new HttpHeaders();
              headers.putAll(super.getHeaders()); // Copy original headers
              headers.add("X-User-Id", userId);    // Add custom header
              headers.add("X-User-Role", role);    // Add custom header
              return headers;
            }
          };

          ServerWebExchange modifiedExchange = exchange.mutate()
              .request(modified)
              .build();

          return chain.filter(modifiedExchange);
        })
        .switchIfEmpty(chain.filter(exchange));
  }
}
