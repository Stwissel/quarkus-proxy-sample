package com.example.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.streams.Pipe;

public enum BodyDispatcher {
  INSTANCE;

  private Map<String, Pipe<Buffer>> pipeMap = new ConcurrentHashMap<>();

  public void registerPipe(String requestId, Pipe<Buffer> pipe) {
    pipeMap.put(requestId, pipe);
  }

  public Pipe<Buffer> getPipe(String requestId) {
    return pipeMap.get(requestId);
  }

  public void removePipe(String requestId) {
    pipeMap.remove(requestId);
  }
}
