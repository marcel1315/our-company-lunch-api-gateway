package com.marceldev.ourcompanylunchapigateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.marceldev.ourcompanylunchapigateway.token.TokenProvider;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
@SpringBootTest(
    classes = OurCompanyLunchApiGatewayApplication.class
)
public class AuthorizationTokenTest {

  @Autowired
  private WebTestClient client;

  @Autowired
  private TokenProvider tokenProvider;

  private MockWebServer mockWebServer;

  @AfterEach
  void tearDown() throws IOException {
    if (mockWebServer != null) {
      mockWebServer.shutdown();
    }
  }

  @DisplayName("Authorization token adds X-User-Id, X-User-Role into header")
  @Test
  void tokenParse() throws IOException, InterruptedException {
    // given
    setUpMockServer(8090, 200, "success");
    String token = tokenProvider.generateToken("hello@email.com", "user");

    // when // then
    client.get()
        .uri("/notifications/fcm")
        .header("Authorization", "Bearer " + token)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .isEqualTo("success");

    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getHeader("X-User-Id")).isEqualTo("hello@email.com");
    assertThat(recordedRequest.getHeader("X-User-Role")).isEqualTo("user");
    assertThat(recordedRequest.getPath()).isEqualTo("/notifications/fcm");
  }

  @DisplayName("Invalid authorization token makes error")
  @Test
  void invalidAuthorizationToken() throws IOException {
    // given
    setUpMockServer(8090, 200, "success");
    String token = "invalid_token";

    // when // then
    client.get()
        .uri("/notifications/fcm")
        .header("Authorization", "Bearer " + token)
        .exchange()
        .expectStatus().is5xxServerError();

    assertThat(mockWebServer.getRequestCount()).isZero();
  }

  @DisplayName("Empty authorization token makes error")
  @Test
  void emptyAuthorizationToken() throws IOException {
    // given
    setUpMockServer(8090, 200, "success");

    // when // then
    client.get()
        .uri("/notifications/fcm")
        .header("Authorization", "Bearer ")
        .exchange()
        .expectStatus().is5xxServerError();

    assertThat(mockWebServer.getRequestCount()).isZero();
  }

  @DisplayName("No authorization header makes unauthorized error")
  @Test
  void noAuthorizationHeader() throws IOException, InterruptedException {
    // given
    setUpMockServer(8090, 200, "success");

    // when // then
    client.get()
        .uri("/notifications/fcm")
        .exchange()
        .expectStatus().isUnauthorized();

    assertThat(mockWebServer.getRequestCount()).isZero();
  }

  private void setUpMockServer(int port, int code, String body) throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start(port);
    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(code)
            .setBody(body)
    );
  }
}
