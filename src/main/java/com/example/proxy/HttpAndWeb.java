package com.example.proxy;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.streams.Pipe;
import io.vertx.ext.web.client.WebClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class HttpAndWeb {

  private HttpClient httpClient;
  private WebClient webClient;

  @Inject
  Vertx vertx;

  public HttpAndWeb() {}

  @PostConstruct
  void init() {
    HttpClientOptions httpOptions = new HttpClientOptions()
        .setSsl(true)
        .setTrustAll(true); // for examples only; remove in production

    this.httpClient = vertx.createHttpClient(httpOptions);
    this.webClient = WebClient.create(vertx);
  }

  public Future<String> demoWebClient() {
    Promise<String> promise = Promise.promise();

    // Example usage of WebClient
    webClient.getAbs("https://httpbin.org/get")
        .send()
        .expecting(HttpResponseExpectation.SC_OK)
        .expecting(HttpResponseExpectation.JSON)
        .onSuccess(response -> promise
            .complete("Received response with status code " + response.statusCode()))
        .onFailure(promise::fail);

    return promise.future();
  }

  Future<HttpClientResponse> httpResponse() {
    Promise<HttpClientResponse> promise = Promise.promise();

    RequestOptions options = new RequestOptions()
        .setMethod(io.vertx.core.http.HttpMethod.GET)
        .setHost("httpbin.org")
        .setPort(443)
        .setURI("/get")
        .setSsl(true);

    httpClient.request(options)
        .compose(request -> request.send())
        .onSuccess(promise::complete)
        .onFailure(promise::fail);

    return promise.future();
  }

  public Future<Pipe<Buffer>> demoHttpClient() {

    Promise<Pipe<Buffer>> promise = Promise.promise();

    httpResponse()
        .onFailure(promise::fail)
        .onSuccess(response -> {
          if (response.statusCode() != 200) {
            promise.fail("Unexpected status code: " + response.statusCode());
            return;
          }
          System.out
              .println("HTTP Client received response with status code " + response.statusCode());
          promise.complete(response.pipe());
        });

    return promise.future();
  }
}
