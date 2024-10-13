package com.example.starter;

import com.example.starter.verticle.HttpServerVerticle;
import com.example.starter.verticle.WorkerVerticle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MainVerticle extends AbstractVerticle {


  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  private AtomicInteger counter = new AtomicInteger(1);
  private Map<Integer, GestionFichiers> listeGestionFichiers = new ConcurrentHashMap<Integer, GestionFichiers>();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

//    int port = 7070;
    int port=7071;

    String logFactory = System.getProperty("org.vertx.logger-delegate-factory-class-name");
    if (logFactory == null) {
      System.setProperty("org.vertx.logger-delegate-factory-class-name",
        SLF4JLogDelegateFactory.class.getName());
    }

    HttpServer server = vertx.createHttpServer();

    if(false) {
      // Déployer le verticle HTTP
      vertx.deployVerticle(new HttpServerVerticle());

      // Déployer le worker verticle
//    DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true);
      DeploymentOptions workerOptions = new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER);
      vertx.deployVerticle(new WorkerVerticle(), workerOptions);
    } else {

      Router router = Router.router(vertx);

//    router.route().handler(LogHandler.create());
      LoggerFormat loggerFormat = LoggerFormat.DEFAULT;
      router.route().handler(RequestLogHandler.create(loggerFormat));
      router.route().handler(BodyHandler.create());

//    router.route().handler(ctx -> {
//
//      // This handler will be called for every request
//      HttpServerResponse response = ctx.response();
//      response.putHeader("content-type", "text/plain");
//
//      // Write to the response and end it
//      response.end("Hello World from Vert.x-Web!");
//    });

      router.route("/init").method(HttpMethod.POST).blockingHandler(
        ctx -> {
          int nb = counter.getAndIncrement();

          MultiMap attributes = ctx.request().formAttributes();
          LOGGER.atInfo().log("init form: {}", attributes.entries());

          HttpServerResponse response = ctx.response();
          response.putHeader("content-type", "text/plain");
//      var res=Future.succeededFuture(nb);
          LOGGER.atInfo().log("init id: {}", nb);
          var gestion = new GestionFichiers(nb);
//        gestion.id = no;
          listeGestionFichiers.put(nb, gestion);
          LOGGER.info("creation de la session {}", nb);
          response.end("" + nb);
        }
      );


      router.route("/listeFichiers/:id").method(HttpMethod.POST).blockingHandler(
        ctx -> {
//        int nb = counter.getAndIncrement();
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
            if (id > 0 && listeGestionFichiers.containsKey(id)) {

              MultiMap attributes = ctx.request().formAttributes();
              LOGGER.atInfo().log("listeFichiers form: {}", attributes.entries());
              var s = attributes.get("data");
              ObjectMapper mapper = new ObjectMapper();
              if (s != null) {
                try {
                  ListFiles2 liste = mapper.readValue(s, ListFiles2.class);

                  tmp = listeGestionFichiers.get(id).listeFichiers(liste);
                  LOGGER.info("listeFichiers $id OK");
                } catch (IOException | NoSuchAlgorithmException e) {
                  LOGGER.error("erreur pour lire le data", e);
                }
              } else {
                LOGGER.error("s est vide: '{}'", s);
                //LOGGER.error("body : '{}'", ctx.request().body());
              }
            } else {
              LOGGER.info("pas de traitement pour $id");
            }
          }
          ObjectMapper objectMapper = new ObjectMapper();
          var res = "";
          try {
            res = objectMapper.writeValueAsString(tmp);
          } catch (JsonProcessingException e) {
            LOGGER.atError().log("JsonProcessingException", e);
          }
          response.end(res);
        }
      );

      router.route("/upload/:id").method(HttpMethod.POST).blockingHandler(
        ctx -> {
//        int nb = counter.getAndIncrement();
          String res = "";
          String idStr = ctx.pathParam("id");
          LOGGER.atInfo().log("upload id: {}", idStr);
          LOGGER.info("upload $idStr ...");
          //var idStr=attributes.get("data");
          if (idStr != null && !idStr.isBlank()) {
            var id = Integer.parseInt(idStr);
            if (id > 0 && listeGestionFichiers.containsKey(id)) {
              MultiMap attributes = ctx.request().formAttributes();
              try {
                res = listeGestionFichiers.get(id).upload(attributes);
              } catch (IOException e) {
                LOGGER.error("erreur pour uploader le fichier", e);
              }
              LOGGER.info("upload {} OK", idStr);
            } else {
              LOGGER.info("pas de traitement pour {}", idStr);
            }
          }
          HttpServerResponse response = ctx.response();
          response.putHeader("content-type", "text/plain");
//      var res=Future.succeededFuture(nb);
          response.end(res);
        }
      );

//    router
//      .post("/init")
//      // this handler will ensure that the response is serialized to json
//      // the content type is set to "application/json"
//      .respond(
//        ctx -> {
//          int nb = counter.getAndIncrement();
//          return Future.succeededFuture(nb);
//        });

      server.requestHandler(router).listen(port)
        .onComplete(http -> {
          if (http.succeeded()) {
            startPromise.complete();
            System.out.println("HTTP server started on port " + port);
          } else {
            startPromise.fail(http.cause());
          }
        });
    }

//    vertx.createHttpServer().requestHandler(req -> {
//      req.response()
//        .putHeader("content-type", "text/plain")
//        .end("Hello from Vert.x!");
//    }).listen(8888).onComplete(http -> {
//      if (http.succeeded()) {
//        startPromise.complete();
//        System.out.println("HTTP server started on port 8888");
//      } else {
//        startPromise.fail(http.cause());
//      }
//    });
  }


  public static Config getConfig() throws IOException {
    var props = new Properties();
    var f = Paths.get("data/config.properties");
    try (var input = Files.newInputStream(f)) {
      props.load(input);
    }
    var rep = props.getProperty("rep", "");
    return new Config(rep);
  }

}

