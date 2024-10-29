package com.marceldev.ourcompanylunchapigateway.config;

import com.marceldev.ourcompanylunchapigateway.token.JwtAuthenticationConverter;
import com.marceldev.ourcompanylunchapigateway.token.JwtReactiveAuthenticationManager;
import com.marceldev.ourcompanylunchapigateway.token.TokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;

@Configuration
public class AppConfig {

  @Value("${common.jwt.secret}")
  private String secret;

  @Value("${common.jwt.expired-in-hour}")
  private int expiredInHour;

  @Bean
  public AuthenticationWebFilter jwtAuthenticationWebFilter() {
    AuthenticationWebFilter filter = new AuthenticationWebFilter(
        jwtAuthenticationManager());
    filter.setServerAuthenticationConverter(new JwtAuthenticationConverter());
    filter.setSecurityContextRepository(securityContextRepository());
    return filter;
  }

  @Bean
  public ReactiveAuthenticationManager jwtAuthenticationManager() {
    return new JwtReactiveAuthenticationManager(tokenProvider());
  }

  @Bean
  public ServerSecurityContextRepository securityContextRepository() {
    return new WebSessionServerSecurityContextRepository();
  }

  @Bean
  public TokenProvider tokenProvider() {
    return new TokenProvider(expiredInHour, secret);
  }
}
