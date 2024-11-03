package com.example.starter.verticle;

import com.example.starter.GestionFichiers;
import com.example.starter.ListFiles2;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Verify;
import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkerVerticle.class);
  public static final String WORKER_LISTE_FICHIERS = "worker.listeFichiers";
  public static final String WORKER_INIT = "worker.init";
  public static final String WORKER_TASK = "worker.task";
  public static final String WORKER_UPLOAD = "worker.upload";


  private AtomicInteger counter = new AtomicInteger(1);
  private Map<Integer, GestionFichiers> listeGestionFichiers = new ConcurrentHashMap<Integer, GestionFichiers>();


  @Override
  public void start() {
    // Ecouter les messages envoyés à l'Event Bus
    vertx.eventBus().consumer(WORKER_TASK, message -> {
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

    vertx.eventBus().consumer(WORKER_INIT, message -> {
      int nb = counter.getAndIncrement();

      String code = (String) message.body();
      LOGGER.atInfo().log("worker.init code: {}", code);
      Verify.verifyNotNull(code, "code");
//      MultiMap attributes = ctx.request().formAttributes();
//      LOGGER.atInfo().log("init form: {}", attributes.entries());

//      HttpServerResponse response = ctx.response();
//      response.putHeader("content-type", "text/plain");
//      var res=Future.succeededFuture(nb);
      LOGGER.atInfo().log("init id: {}", nb);
      var gestion = new GestionFichiers(nb, code);
//        gestion.id = no;
      listeGestionFichiers.put(nb, gestion);
      LOGGER.info("creation de la session {}", nb);
      message.reply("" + nb);
    });

    vertx.eventBus().consumer(WORKER_LISTE_FICHIERS, message -> {
      Object tab[] = (Object[]) message.body();
      LOGGER.info("listeFichiers tab={}", tab);
      var tmp = new ListFiles2();
      tmp.setListe(new ArrayList<>());
      tmp.setCode("");
//        logger.info("liste fichier $idStr ...")
//      if (idStr != null && !idStr.isBlank()) {
      var id = (Integer) tab[0];
      if (id > 0 && listeGestionFichiers.containsKey(id)) {

//          MultiMap attributes = ctx.request().formAttributes();
//          LOGGER.atInfo().log("listeFichiers form: {}", attributes.entries());
//          var s = attributes.get("data");
        ListFiles2 liste = (ListFiles2) tab[1];
        ObjectMapper mapper = new ObjectMapper();
        if (true) {
          try {
//              ListFiles2 liste = mapper.readValue(s, ListFiles2.class);

            tmp = listeGestionFichiers.get(id).listeFichiers(liste);
            LOGGER.info("listeFichiers {} OK", id);
          } catch (IOException | NoSuchAlgorithmException e) {
            LOGGER.error("erreur pour lire le data", e);
          }
        } else {
          LOGGER.error("s est vide: '{}'", "");
          //LOGGER.error("body : '{}'", ctx.request().body());
        }
      } else {
        LOGGER.info("pas de traitement pour {}", id);
      }
//      }
//      ObjectMapper objectMapper = new ObjectMapper();
//      var res = "";
//      try {
//        res = objectMapper.writeValueAsString(tmp);
//      } catch (JsonProcessingException e) {
//        LOGGER.atError().log("JsonProcessingException", e);
//      }
      message.reply(tmp);
    });


    vertx.eventBus().consumer(WORKER_UPLOAD, message -> {

      Object tab[] = (Object[]) message.body();
      int id = (Integer) tab[0];
      String file = (String) tab[1];
      String filename = (String) tab[2];
      String res = "";
      if (id > 0 && listeGestionFichiers.containsKey(id)) {
        try {
          res = listeGestionFichiers.get(id).upload(file, filename);
        } catch (IOException e) {
          LOGGER.error("erreur pour uploader le fichier", e);
        }
        LOGGER.info("upload {} OK", id);
      } else {
        LOGGER.info("pas de traitement pour {}", id);
      }

//      int nb = counter.getAndIncrement();

//      MultiMap attributes = ctx.request().formAttributes();
//      LOGGER.atInfo().log("init form: {}", attributes.entries());

//      HttpServerResponse response = ctx.response();
//      response.putHeader("content-type", "text/plain");
//      var res=Future.succeededFuture(nb);
//      LOGGER.atInfo().log("init id: {}", nb);
//      var gestion = new GestionFichiers(nb);
////        gestion.id = no;
//      listeGestionFichiers.put(nb, gestion);
//      LOGGER.info("creation de la session {}", nb);
      message.reply(res);
    });

  }

}
