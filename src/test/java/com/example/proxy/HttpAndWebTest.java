package com.example.proxy;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.Pipe;
import jakarta.inject.Inject;

@QuarkusTest
public class HttpAndWebTest {

  @Inject
  HttpAndWeb httpAndWeb;

  @Test
  public void demoWebClient_returns200() throws Exception {
    Future<String> fut = httpAndWeb.demoWebClient();

    CompletableFuture<String> cf = new CompletableFuture<>();
    fut.onSuccess(cf::complete).onFailure(cf::completeExceptionally);

    String result = cf.get(15, TimeUnit.SECONDS);
    assertTrue(result.contains("Received response with status code"));
  }

  @Test
  public void demoHttpClient_returnsPipe() throws Exception {
    Future<Pipe<Buffer>> fut = httpAndWeb.demoHttpClient();

    CompletableFuture<Pipe<Buffer>> cf = new CompletableFuture<>();
    fut.onSuccess(cf::complete).onFailure(cf::completeExceptionally);

    Pipe<Buffer> pipe = cf.get(15, TimeUnit.SECONDS);
    assertTrue(pipe != null);

    pipe.to(null);
  }


}
