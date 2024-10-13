package com.example.starter.verticle;

import com.example.starter.GestionFichiers;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkerVerticle.class);


  private AtomicInteger counter = new AtomicInteger(1);
  private Map<Integer, GestionFichiers> listeGestionFichiers = new ConcurrentHashMap<Integer, GestionFichiers>();


  @Override
  public void start() {
    // Ecouter les messages envoyés à l'Event Bus
    vertx.eventBus().consumer("worker.task", message -> {
      // Simuler une tâche bloquante (exemple: appel à un service ou calcul lourd)
      LOGGER.info("Worker received task: " + message.body());

      try {
        Thread.sleep(5000);  // Simuler une opération bloquante
      } catch (InterruptedException e) {
        LOGGER.error(e.getMessage(), e);
      }

      // Retourner le résultat après le traitement
      message.reply("Task completed for: " + message.body());
    });

    vertx.eventBus().consumer("worker.init", message -> {
      int nb = counter.getAndIncrement();

//      MultiMap attributes = ctx.request().formAttributes();
//      LOGGER.atInfo().log("init form: {}", attributes.entries());

//      HttpServerResponse response = ctx.response();
//      response.putHeader("content-type", "text/plain");
//      var res=Future.succeededFuture(nb);
      LOGGER.atInfo().log("init id: {}", nb);
      var gestion = new GestionFichiers(nb);
//        gestion.id = no;
      listeGestionFichiers.put(nb, gestion);
      LOGGER.info("creation de la session {}", nb);
      message.reply("" + nb);
    });

  }

}
