package com.example.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/proxy")
public class ProxyResource {

  @Inject
  EventBus eventBus;
  @Inject
  ObjectMapper mapper;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_HTML)
  public Uni<Response> proxy(ProxyRequest request) {
    String url = request != null ? request.url : null;
    if (url == null || url.isBlank()) {
      return Uni.createFrom().failure(new IllegalArgumentException("url is required"));
    }
    return eventBus
        .request(UrlProcessorVerticle.ADDRESS, url)
        .onItem().transform(m -> resultConverter((JsonObject) m.body()));
  }

  Response resultConverter(JsonObject json) {
    ProxyResult result = mapper.convertValue(json, ProxyResult.class);
    Response.ResponseBuilder responseBuilder = Response.status(result.statusCode);
    if (result.headers != null) {
      result.headers.forEach((key, value) -> responseBuilder.header(key, value));
    }
    return responseBuilder.entity(result.body).build();
  }
}
