package com.example.proxy;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import io.vertx.mutiny.core.http.HttpClient;
import io.vertx.mutiny.core.http.HttpClientRequest;

public class UrlProcessorVerticleNeo extends
    AbstractVerticle {

  public static final String NEOADDRESS = "neoprocessurl";
  private HttpClient httpClient;

  @Override
  public Uni<Void> asyncStart() {

    httpClient = vertx.createHttpClient();
    EventBus eb = vertx.eventBus();
    eb.<String>consumer(NEOADDRESS, this::onNeoProcessUrl);
    return Uni.createFrom().voidItem();
  }

  private void onNeoProcessUrl(Message<String> msg) {
    String urlString = msg.body();
    if (urlString == null || urlString.isBlank()) {
      msg.fail(400, "url is required");
      return;
    }

    String uuid = msg.headers().get("requestid");

    String host;
    int port;
    String requestUri;
    boolean isSsl;

    try {
      URL url = URI.create(urlString).toURL();
      isSsl = "https".equals(url.getProtocol());
      host = url.getHost();
      port = url.getPort() == -1
          ? isSsl ? 443 : 80
          : url.getPort();
      requestUri = url.getPath();

    } catch (MalformedURLException e) {
      msg.fail(400, "Invalid URL");
      return;
    }

    RequestOptions reqOptions = new RequestOptions()
        .setHost(host)
        .setPort(port)
        .setURI(requestUri)
        .setSsl(isSsl)
        .setFollowRedirects(true)
        .setTimeout(15_000);

    httpClient.request(reqOptions)
        .flatMap(HttpClientRequest::send)
        .subscribe().with(resp -> {
          if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            // We are good
            BodyDispatcher.INSTANCE.registerPipe(uuid, resp.pipe());
            JsonObject headers = new JsonObject();
            resp.headers()
                .forEach(header -> headers.put(header.getKey().toLowerCase(), header.getValue()));
            JsonObject responseJson = new JsonObject()
                .put("status", "pipe registered")
                .put("requestid", uuid)
                .put("headers", headers);
            msg.reply(responseJson);
          } else {
            // It sucked
            msg.fail(500, " STATUS CODE " + resp.statusCode());
          }
        },
            err -> msg.fail(500, err.getMessage()));
  }

}
