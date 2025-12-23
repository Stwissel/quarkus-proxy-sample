package com.example.proxy;

import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResultStreamManager {

  private final Map<String, StringBuilder> resultStreams =
      new java.util.concurrent.ConcurrentHashMap<>();

  public String get(String id) {
    StringBuilder sb = resultStreams.get(id);
    return sb == null ? null : sb.toString();
  }

  public void append(String id, String chunk) {
    resultStreams.compute(id, (k, v) -> {
      if (v == null)
        v = new StringBuilder();
      v.append(chunk);
      return v;
    });
  }

  public void clear(String id) {
    resultStreams.remove(id);
  }

}
