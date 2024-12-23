package com.example.starter.verticle;

import com.example.starter.GestionFichiers;
import com.example.starter.ListFiles2;
import com.example.starter.MainVerticle;
import com.example.starter.RequestLogHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.base.Verify;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpServerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);


//  private AtomicInteger counter = new AtomicInteger(1);
//  private Map<Integer, GestionFichiers> listeGestionFichiers = new ConcurrentHashMap<Integer, GestionFichiers>();



  @Override
  public void start() {

//    int port = 7070;
    int port=7071;

    Router router = Router.router(vertx);

    LoggerFormat loggerFormat = LoggerFormat.DEFAULT;
    router.route().handler(RequestLogHandler.create(loggerFormat));
    router.route().handler(BodyHandler.create());

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

    router.route("/init").method(HttpMethod.POST).blockingHandler(
      ctx -> {
//        int nb = counter.getAndIncrement();



        MultiMap attributes = ctx.request().formAttributes();
        LOGGER.atInfo().log("init form: {}", attributes.entries());

        var code="";
        if(attributes.contains("data")){
          var body=attributes.get("data");
          try {
            ObjectMapper objectMapper = new ObjectMapper();
            var node=objectMapper.reader().readTree(body);
            if(node.has("noSauve")){
              code=node.get("noSauve").asText();
            }
          }catch(JsonProcessingException e){
            LOGGER.atError().log("Erreur pour parser le flux: {}", body, e);
          }
        }
        Verify.verify(!Strings.isNullOrEmpty(code), "noSauve is empty");
//        HttpServerResponse response = ctx.response();
//        response.putHeader("content-type", "text/plain");
//      var res=Future.succeededFuture(nb);
//        LOGGER.atInfo().log("init id: {}", nb);
//        var gestion = new GestionFichiers(nb);
////        gestion.id = no;
//        listeGestionFichiers.put(nb, gestion);

        final var code2=code;

        vertx.eventBus().request(WorkerVerticle.WORKER_INIT, code, reply -> {
          if (reply.succeeded()) {
            var nb="";
            LOGGER.info("creation de la session {} (code:{})", nb, code2);
            nb=(String) reply.result().body();
            ctx.response()
              .putHeader("content-type", "text/plain")
              .end(nb);
          } else {
            ctx.response().setStatusCode(500).end("Erreur lors du traitement.");
          }
        });

//        LOGGER.info("creation de la session {}", nb);
//        response.end("" + nb);
      }
    );


    router.route("/listeFichiers/:id").method(HttpMethod.POST).blockingHandler(
      ctx -> {
//        int nb = counter.getAndIncrement();
        var res = "";
        String idStr = ctx.pathParam("id");
        LOGGER.atInfo().log("listeFichiers id: {}", idStr);
        HttpServerResponse response = ctx.response();
        response.putHeader("content-type", "text/plain");
//      var res=Future.succeededFuture(nb);
        var tmp = new ListFiles2();
        tmp.setListe(new ArrayList<>());
        tmp.setCode("");
//        logger.info("liste fichier $idStr ...")
        if (idStr != null && !idStr.isBlank()) {
          var id = Integer.parseInt(idStr);
          if (id > 0) {

            MultiMap attributes = ctx.request().formAttributes();
            LOGGER.atInfo().log("listeFichiers form: {}", attributes.entries());
            var s = attributes.get("data");
            ObjectMapper mapper = new ObjectMapper();
            if (s != null) {
              try {
                ListFiles2 liste = mapper.readValue(s, ListFiles2.class);

                Object tab[]={id,liste};
                vertx.eventBus().request(WorkerVerticle.WORKER_LISTE_FICHIERS, tab, reply -> {
                  if (reply.succeeded()) {
//                    var nb="";
//                    LOGGER.info("creation de la session {}", nb);
                    var tmp2=(ListFiles2) reply.result().body();
                    if(tmp2!=null) {
                      tmp.setCode(tmp2.getCode());
                      tmp.setListe(tmp2.getListe());
                    }
                    LOGGER.info("réponse tmp: {}",tmp);
                    LOGGER.info("réponse tmp2: {}",tmp2);
//                    ctx.response()
//                      .putHeader("content-type", "text/plain")
//                      .end(nb);
                  } else {
                    ctx.response().setStatusCode(500).end("Erreur lors du traitement.");
                  }
                });

                //tmp = listeGestionFichiers.get(id).listeFichiers(liste);
                //LOGGER.info("listeFichiers $id OK");
              } catch (IOException e) {
                LOGGER.error("erreur pour lire le data", e);
              }
            } else {
              LOGGER.error("s est vide: '{}'", s);
              //LOGGER.error("body : '{}'", ctx.request().body());
            }
          } else {
            LOGGER.info("pas de traitement pour {}",id);
          }
        }
        ObjectMapper objectMapper = new ObjectMapper();
//        var res = "";
        LOGGER.info("réponse tmp json: {}",tmp);
        try {
          res = objectMapper.writeValueAsString(tmp);
        } catch (JsonProcessingException e) {
          LOGGER.atError().log("JsonProcessingException", e);
        }
        LOGGER.info("réponse res: {}",res);
        response.end(res);


      }
    );

    router.route("/upload/:id").method(HttpMethod.POST).blockingHandler(
      ctx -> {
//        int nb = counter.getAndIncrement();
        final StringBuffer res = new StringBuffer();
        String idStr = ctx.pathParam("id");
        LOGGER.atInfo().log("upload id: {}", idStr);
        LOGGER.info("upload {} ...",idStr);
        //var idStr=attributes.get("data");
        if (idStr != null && !idStr.isBlank()) {
          var id = Integer.parseInt(idStr);
          if (id > 0) {
            MultiMap attributes = ctx.request().formAttributes();
            var file=attributes.get("file");
            var filename=attributes.get("filename");
            Object tab[]={id,file,filename};
            vertx.eventBus().request(WorkerVerticle.WORKER_UPLOAD, tab, reply -> {
              if (reply.succeeded()) {
//                    var nb="";
//                    LOGGER.info("creation de la session {}", nb);
                var res2=(String) reply.result().body();
                res.setLength(0);
                res.append(res2);
//                    ctx.response()
//                      .putHeader("content-type", "text/plain")
//                      .end(nb);
              } else {
                ctx.response().setStatusCode(500).end("Erreur lors du traitement.");
              }
            });
//            try {
//              res = listeGestionFichiers.get(id).upload(attributes);
//            } catch (IOException e) {
//              LOGGER.error("erreur pour uploader le fichier", e);
//            }
//            LOGGER.info("upload {} OK", idStr);
          } else {
            LOGGER.info("pas de traitement pour {}", idStr);
          }
        }
        HttpServerResponse response = ctx.response();
        response.putHeader("content-type", "text/plain");
//      var res=Future.succeededFuture(nb);
        response.end(res.toString());
      }
    );

    // Créer le serveur HTTP
    HttpServerOptions options = new HttpServerOptions().setMaxFormAttributeSize(10 * 1024 * 1024);
    vertx.createHttpServer(options).requestHandler(router).listen(port, res -> {
      if (res.succeeded()) {
        LOGGER.info("HTTP server démarré sur le port {}",port);
      } else {
        LOGGER.info("Erreur lors du démarrage du serveur HTTP: {}",res.cause());
      }
    });
  }
}
