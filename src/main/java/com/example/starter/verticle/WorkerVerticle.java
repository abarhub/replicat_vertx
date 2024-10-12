package com.example.starter.verticle;

import io.vertx.core.AbstractVerticle;

public class WorkerVerticle extends AbstractVerticle {

  @Override
  public void start() {
    // Ecouter les messages envoyés à l'Event Bus
    vertx.eventBus().consumer("worker.task", message -> {
      // Simuler une tâche bloquante (exemple: appel à un service ou calcul lourd)
      System.out.println("Worker received task: " + message.body());

      try {
        Thread.sleep(5000);  // Simuler une opération bloquante
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      // Retourner le résultat après le traitement
      message.reply("Task completed for: " + message.body());
    });
  }

}
