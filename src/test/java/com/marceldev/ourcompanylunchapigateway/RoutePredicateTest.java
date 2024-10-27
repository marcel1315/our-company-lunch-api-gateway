package com.marceldev.ourcompanylunchapigateway;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
@SpringBootTest(
    classes = OurCompanyLunchApiGatewayApplication.class
)
public class RoutePredicateTest {

  @Autowired
  private WebTestClient client;

  private MockWebServer mockWebServer;

  @AfterEach
  void tearDown() throws IOException {
    if (mockWebServer != null) {
      mockWebServer.shutdown();
    }
  }

  @DisplayName("/users goes to 9020 port")
  @Test
  void usersRouteTest() throws InterruptedException, IOException {
    // given
    setUpMockServer(9020, 200, "success");

    // when // then
    client.get()
        .uri("/users/signup")
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .isEqualTo("success");

    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getPath()).isEqualTo("/users/signup");
  }

  @DisplayName("/users without auth will have unauthorized")
  @Test
  void usersUnauthorizedTest() throws InterruptedException, IOException {
    // given
    setUpMockServer(9020, 200, "success");

    // when // then
    client.get()
        .uri("/users/info")
        .exchange()
        .expectStatus().isUnauthorized();

    assertThat(mockWebServer.getRequestCount()).isZero();
  }

  @DisplayName("/notifications goes to 8090 port")
  @Test
  @WithMockUser
  void notiRouteTest() throws InterruptedException, IOException {
    // given
    setUpMockServer(8090, 200, "success");

    // when // then
    client.get()
        .uri("/notifications/fcm")
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .isEqualTo("success");

    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getPath()).isEqualTo("/notifications/fcm");
  }

  @DisplayName("Other paths go to 8085 port by default")
  @Test
  @WithMockUser
  void defaultRouteTest() throws InterruptedException, IOException {
    // given
    setUpMockServer(8085, 200, "success");

    // when // then
    client.get()
        .uri("/diner")
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .isEqualTo("success");

    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    assertThat(recordedRequest.getPath()).isEqualTo("/diner");
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
