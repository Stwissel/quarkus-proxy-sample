package com.example.proxy;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

/**
 * Minimal Vert.x raw HttpClient examples (GET and POST JSON).
 * Run from your IDE or as a simple main class in the project.
 */
public class VertxHttpClientExample {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    HttpClientOptions options = new HttpClientOptions()
        .setSsl(true)
        .setTrustAll(true); // for examples only; remove in production

    HttpClient client = vertx.createHttpClient(options);

    // --- Simple GET using raw HttpClient ---
    client.request(HttpMethod.GET, 443, "httpbin.org", "/get").onComplete(reqAr -> {
      if (reqAr.succeeded()) {
        HttpClientRequest getReq = reqAr.result();
        getReq.exceptionHandler(err -> System.err.println("GET request failed: " + err));
        getReq.send(ar -> {
          if (ar.succeeded()) {
            HttpClientResponse resp = ar.result();
            resp.bodyHandler(body -> {
              System.out.println("GET status: " + resp.statusCode());
              System.out.println("GET body: " + body.toString());
            });
          } else {
            System.err.println("GET send failed: " + ar.cause());
          }
        });
      } else {
        System.err.println("Failed to create GET request: " + reqAr.cause());
      }
    });

    // --- POST JSON using raw HttpClient ---
    JsonObject payload = new JsonObject().put("message", "hello from vertx httpclient");
    client.request(HttpMethod.POST, 443, "httpbin.org", "/post").onComplete(reqAr -> {
      if (reqAr.succeeded()) {
        HttpClientRequest postReq = reqAr.result();
        postReq.exceptionHandler(err -> System.err.println("POST request failed: " + err));
        postReq.putHeader("Content-Type", "application/json");
        Buffer body = Buffer.buffer(payload.encode());
        postReq.putHeader("Content-Length", String.valueOf(body.length()));
        postReq.send(body, ar -> {
          if (ar.succeeded()) {
            HttpClientResponse resp = ar.result();
            resp.bodyHandler(b -> {
              System.out.println("POST status: " + resp.statusCode());
              System.out.println("POST body: " + b.toString());
            });
          } else {
            System.err.println("POST send failed: " + ar.cause());
          }
        });
      } else {
        System.err.println("Failed to create POST request: " + reqAr.cause());
      }
    });

    // Keep the JVM alive briefly to receive async responses, then close
    vertx.setTimer(5000, id -> {
      client.close();
      vertx.close();
    });
  }
}
