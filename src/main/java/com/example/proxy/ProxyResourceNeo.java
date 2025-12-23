package com.example.proxy;

import java.util.Map;
import java.util.UUID;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import io.vertx.mutiny.core.http.HttpServerResponse;
import io.vertx.mutiny.core.streams.Pipe;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("/neoproxy")
public class ProxyResourceNeo {

  @Inject
  EventBus eventBus;

  @POST
  @Path("/neoproxy")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_HTML)
  public Uni<Void> neoproxy(ProxyRequest request, @Context RoutingContext routingContext) {
    io.vertx.mutiny.ext.web.RoutingContext mrc =
        io.vertx.mutiny.ext.web.RoutingContext.newInstance(routingContext);

    String url = request != null ? request.url : null;
    if (url == null || url.isBlank()) {
      return Uni.createFrom().failure(new IllegalArgumentException("url is required"));
    }

    String uuid = UUID.randomUUID().toString();

    DeliveryOptions options = new DeliveryOptions();
    options.setLocalOnly(true)
        .addHeader("requestid", uuid);

    return eventBus.request(UrlProcessorVerticleNeo.NEOADDRESS, url, options)
        .onItem().transformToUni(message -> neoResultConverter(mrc, message));
  }


  Uni<Void> neoResultConverter(io.vertx.mutiny.ext.web.RoutingContext rc, Message<Object> message) {
    HttpServerResponse response = rc.response();
    Object body = message.body();
    if (body instanceof JsonObject json) {
      String requestId = json.getString("requestid");
      Pipe<Buffer> pipe = BodyDispatcher.INSTANCE.getPipe(requestId);
      if (pipe != null) {
        BodyDispatcher.INSTANCE.removePipe(requestId);
        JsonObject headers = json.getJsonObject("headers");
        for (Map.Entry<String, Object> header : headers) {
          response.putHeader(header.getKey(), header.getValue().toString());
        }
        rc.response().setChunked(!headers.containsKey("content-length"));
        return pipe.to(rc.response());
      } else {
        rc.end("No pipe found for requestId: " + requestId);
      }
    } else {
      rc.end("Invalid response");
    }
    return Uni.createFrom().voidItem();
  }

}
