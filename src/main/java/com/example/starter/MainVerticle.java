package com.example.starter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainVerticle extends AbstractVerticle {


  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  private AtomicInteger counter = new AtomicInteger(1);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    int port = 7070;

    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);

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

        HttpServerResponse response = ctx.response();
        response.putHeader("content-type", "text/plain");
//      var res=Future.succeededFuture(nb);
        LOGGER.atInfo().log("init id: {}", nb);
        response.end("" + nb);
      }
    );


    router.route("/listeFichiers/:id").method(HttpMethod.POST).blockingHandler(
      ctx -> {
//        int nb = counter.getAndIncrement();
        String id = ctx.pathParam("id");
        LOGGER.atInfo().log("listeFichiers id: {}", id);
        HttpServerResponse response = ctx.response();
        response.putHeader("content-type", "text/plain");
//      var res=Future.succeededFuture(nb);
        var tmp = new ListFiles2();
        tmp.setListe(new ArrayList<>());
        tmp.setCode("");
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
        String id = ctx.pathParam("id");
        LOGGER.atInfo().log("upload id: {}", id);
        HttpServerResponse response = ctx.response();
        response.putHeader("content-type", "text/plain");
//      var res=Future.succeededFuture(nb);
        response.end("");
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
}

class ListFiles2 {
  private List<String> liste;
  private String code;

  public List<String> getListe() {
    return liste;
  }

  public void setListe(List<String> liste) {
    this.liste = liste;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}

