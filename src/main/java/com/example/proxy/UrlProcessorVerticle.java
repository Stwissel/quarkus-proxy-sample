package com.example.proxy;

import java.util.HashMap;
import java.util.Map;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;

public class UrlProcessorVerticle extends AbstractVerticle {

  public static final String ADDRESS = "processurl";
  private WebClient webClient;


  @Override
  public void start(Promise<Void> startPromise) {
    WebClientOptions options = new WebClientOptions()
        .setFollowRedirects(true)
        .setTrustAll(true)
        .setUserAgent("quarkus-proxy-sample/1.0")
        .setConnectTimeout(5_000)
        .setKeepAlive(true);
    this.webClient = WebClient.create(vertx, options);


    EventBus eb = vertx.eventBus();
    eb.<String>consumer(ADDRESS, this::onProcessUrl);
    startPromise.complete();
  }

  private void onProcessUrl(Message<String> msg) {
    String url = msg.body();
    if (url == null || url.isBlank()) {
      msg.fail(400, "url is required");
      return;
    }

    webClient
        .requestAbs(HttpMethod.GET, url)
        .timeout(15_000)
        .as(BodyCodec.buffer())
        .send()
        .onSuccess(resp -> {
          ProxyResult result = toResult(resp);
          msg.reply(JsonObject.mapFrom(result));
        })
        .onFailure(err -> {
          ProxyResult result = new ProxyResult();
          result.statusCode = 0;
          result.headers = java.util.Collections.emptyMap();
          result.body = "error: " + err.getClass().getSimpleName() + ": " + err.getMessage();
          msg.reply(JsonObject.mapFrom(result));
        });
  }

  private ProxyResult toResult(HttpResponse<Buffer> resp) {
    ProxyResult pr = new ProxyResult();
    pr.statusCode = resp.statusCode();
    Map<String, String> headers = new HashMap<>();
    resp.headers().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
    pr.headers = headers;
    pr.body = resp.bodyAsString();
    return pr;
  }
}
