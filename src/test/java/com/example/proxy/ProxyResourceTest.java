package com.example.proxy;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.vertx.mutiny.core.Vertx;
import jakarta.inject.Inject;

@QuarkusTest
public class ProxyResourceTest {

  @Inject
  Vertx vertx;

  @BeforeEach
  public void ensureVerticleDeployed() {
    // Deploy the verticle for tests to ensure EventBus consumer is present
    vertx.deployVerticle(new UrlProcessorVerticle()).await().indefinitely();
  }

  @Test
  public void postProxy_fetchesUrlAndReturnsResult() {
    given()
        .contentType(ContentType.JSON)
        .body(new ProxyRequest() {
          {
            url = "https://httpbin.org/get";
          }
        })
        .when()
        .post("/proxy")
        .then()
        .statusCode(200)
        .body("statusCode", is(200))
        .body("headers", notNullValue())
        .body("body", notNullValue());
  }

  @Test
  public void postProxy_missingUrl_returnsError() {
    given()
        .contentType(ContentType.JSON)
        .body(new ProxyRequest())
        .when()
        .post("/proxy")
        .then()
        .statusCode(500); // IllegalArgumentException mapped by default
  }
}
