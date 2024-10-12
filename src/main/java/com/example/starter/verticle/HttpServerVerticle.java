package com.example.starter.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;

public class HttpServerVerticle extends AbstractVerticle {
  @Override
  public void start() {
    Router router = Router.router(vertx);

    // Définir une route pour gérer les requêtes HTTP
    router.route("/task").handler(ctx -> {
      // Envoyer une tâche au worker via l'Event Bus
      vertx.eventBus().request("worker.task", "Processing task for client", reply -> {
        if (reply.succeeded()) {
          ctx.response()
            .putHeader("content-type", "text/plain")
            .end((String) reply.result().body());
        } else {
          ctx.response().setStatusCode(500).end("Erreur lors du traitement.");
        }
      });
    });

    // Créer le serveur HTTP
    vertx.createHttpServer().requestHandler(router).listen(8080, res -> {
      if (res.succeeded()) {
        System.out.println("HTTP server démarré sur le port 8080");
      } else {
        System.out.println("Erreur lors du démarrage du serveur HTTP: " + res.cause());
      }
    });
  }
}
