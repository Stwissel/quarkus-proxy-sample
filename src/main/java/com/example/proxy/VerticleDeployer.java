package com.example.proxy;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.DeploymentOptions;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class VerticleDeployer {

  @Inject
  Vertx vertx;

  void init(@Observes final StartupEvent e, final Vertx vertx) {
    vertx.deployVerticle(new UrlProcessorVerticle(), new DeploymentOptions())
        .subscribe().with(id -> {
          System.out.println("Deployed UrlProcessorVerticle with id: " + id);
        }, failure -> {
          System.err.println("Failed to deploy UrlProcessorVerticle: " + failure.getMessage());
        });
  }
}
