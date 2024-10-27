package com.marceldev.ourcompanylunchapigateway.token;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

  private final TokenProvider tokenProvider;

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    String token = authentication.getCredentials().toString();

    if (tokenProvider.validateToken(token)) {
      return Mono.just(tokenProvider.getAuthentication(token));
    } else {
      return Mono.empty();
    }
  }
}
