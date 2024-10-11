package com.example.starter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    Router router = Router.router(vertx);

//    router.route().handler(LogHandler.create());
    LoggerFormat loggerFormat = LoggerFormat.DEFAULT;
    router.route().handler(RequestLogHandler.create(loggerFormat));

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
              } catch (IOException e) {
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

class Config {

  private String rep;

  public Config(String rep) {
    this.rep = rep;
  }

  public String getRep() {
    return rep;
  }

  public void setRep(String rep) {
    this.rep = rep;
  }
}

class ListFiles2 {
  private List<Files2> liste;
  private String code;

  public ListFiles2() {
  }

  public ListFiles2(List<Files2> liste, String code) {
    this.liste = liste;
    this.code = code;
  }

  public List<Files2> getListe() {
    return liste;
  }

  public void setListe(List<Files2> liste) {
    this.liste = liste;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}

class Files2 {
  private String filename;
  private long size;
  private String hash;
  private String type;

  public Files2() {
  }

  public Files2(String filename, long size, String hash, String type) {
    this.filename = filename;
    this.size = size;
    this.hash = hash;
    this.type = type;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}

class GestionFichiers {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);


  private int id = 0;

  public GestionFichiers(int id) {
    this.id = id;
  }

  public ListFiles2 listeFichiers(ListFiles2 listeFiles2) throws IOException {
//    val body = ctx.body()
    LOGGER.info("request3 is $body");
    var res = "";

//    if (body != null && body.contains("=")) {
//      val s = body.substring(body.indexOf("=") + 1)
//
    var config = MainVerticle.getConfig();
//
//      val s4 = ctx.formParam("data")
//      if (s4 != null) {
//        logger.info("s4 is '$s4'")
//        val s2 = Json.decodeFromString<ListFiles2>(s4)
//          logger.info("s2 is $s2")

    var liste = new ArrayList<Files2>();

    var parent = Paths.get(config.getRep());
    for (var file : listeFiles2.getListe()) {
      var f = Paths.get(config.getRep(), file.getFilename());
      if (!f.startsWith(parent)) {
        LOGGER.error("Le chemin est invalide ${file.filename} parent=${parent}");
        throw new IOException("Erreur dans le chemin");
      }
      if (Files.exists(f)) {
        LOGGER.info("$f is exists");
        if (Files.isDirectory(f)) {
          throw new IOException("$f is a directory");
        }
        var contenu = Files.readAllBytes(f);
        if (file.getHash().isEmpty()) {
          LOGGER.info("$f n'a pas de hash => on l'importe");
          liste.add(new Files2(file.getFilename(), 0, "", "F"));
        } else {
          //var hash = hashString(contenu, "SHA-256").toHex();
          var hash = hashString(contenu);
          if (hash.equals(file.getHash())) {
            LOGGER.info("$f est identique");
          } else {
            LOGGER.info("$f est different => on l'importe");
            liste.add(new Files2(file.getFilename(), 0, "", "F"));
          }
        }
      } else {
        LOGGER.info("$f is not exists");
        liste.add(new Files2(file.getFilename(), 0, "", "F"));
      }
    }

    var liste2 = new ListFiles2(liste, "");
//        var s5 = Json.encodeToString(liste2);
//        res = s5
//      }
//    }
//    ctx.result(res)
    return liste2;
  }


//  fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }
//
//  fun hashString(str: ByteArray, algorithm: String): ByteArray =
//    MessageDigest.getInstance(algorithm).digest(str)

  private String hashString(byte[] buf) {
    return null;
  }

  public String upload(MultiMap attributes) throws IOException {
    //val body = ctx.body();
    LOGGER.info("upload size is ${body.length}");
    var res = "KO";

    //if (body.contains("=")) {
    var config = MainVerticle.getConfig();

    //var s4 = ctx.formParam("file");
    var s4 = attributes.get("file");
    if (s4 != null) {

      //var s5 = ctx.formParam("filename");
      var s5 = attributes.get("filename");
      if (s5 != null) {

        var s04 = Base64.getDecoder().decode(s4);

        LOGGER.info("file $s5 size: {}", s4.length());
        var f = Paths.get(config.getRep(), s5);
        LOGGER.info("f is '{}'", f);
        if (Files.notExists(f.getParent())) {
          LOGGER.info("création du répertoire {}", f.getParent());
          Files.createDirectories(f.getParent());
        }
        Files.write(f, s04);
        LOGGER.info("write {}", f);
        res = "OK";
      }


    }
    //}

    //ctx.result(res)
    return res;
  }
}

