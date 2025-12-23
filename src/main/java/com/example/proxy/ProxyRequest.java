package com.example.proxy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.json.JsonObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProxyRequest {
  public String url;

  public static ProxyRequest fromJson(JsonObject json) {
    return json.mapTo(ProxyRequest.class);
  }
}
