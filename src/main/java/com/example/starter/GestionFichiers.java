package com.example.starter;

import com.google.common.base.Verify;
import com.google.common.io.BaseEncoding;
import io.vertx.core.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

public class GestionFichiers {

  private static final Logger LOGGER = LoggerFactory.getLogger(GestionFichiers.class);

  private int id = 0;
  private final String code;

  public GestionFichiers(int id, String code) {
    this.id = id;
    this.code = code;
  }

  public ListFiles2 listeFichiers(ListFiles2 listeFiles2) throws IOException, NoSuchAlgorithmException {
//    val body = ctx.body()
    LOGGER.info("request3 is {}", listeFiles2);
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
      /*if (!f.startsWith(parent)) {
        LOGGER.error("Le chemin est invalide {} parent={}",f,parent);
        throw new IOException("Erreur dans le chemin");
      }*/
      if (Files.exists(f)) {
        LOGGER.info("{} is exists",f);
        if (Files.isDirectory(f)) {
          throw new IOException(f+" is a directory");
        }
        var contenu = Files.readAllBytes(f);
        if (file.getHash().isEmpty()) {
          LOGGER.info("{} n'a pas de hash => on l'importe",f);
          liste.add(new Files2(file.getFilename(), 0, "", "F"));
        } else {
          //var hash = hashString(contenu, "SHA-256").toHex();
          var hash = hashString(contenu);
          if (hash.equals(file.getHash())) {
            LOGGER.info("{} est identique",f);
          } else {
            LOGGER.info("{} est different => on l'importe",f);
            liste.add(new Files2(file.getFilename(), 0, "", "F"));
          }
        }
      } else {
        LOGGER.info("{} is not exists",f);
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

  private String hashString(byte[] buf) throws NoSuchAlgorithmException {
    var b = MessageDigest.getInstance("SHA-256").digest(buf);
    return BaseEncoding.base16().lowerCase().encode(b);
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

        LOGGER.info("file {} size: {}", s5,s4.length());
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

  public String upload(String file,String filename) throws IOException {
    //val body = ctx.body();
    LOGGER.info("upload size is ${body.length}");
    var res = "KO";

    //if (body.contains("=")) {
    var config = MainVerticle.getConfig();

    //var s4 = ctx.formParam("file");
    var s4 = file;
    if (s4 != null) {

      //var s5 = ctx.formParam("filename");
      var s5 = filename;
      if (s5 != null) {

        var s04 = Base64.getDecoder().decode(s4);

        LOGGER.info("file {} size: {}", s5,s4.length());
        LOGGER.info("rep: '{}'", config.getRep());
        Verify.verify(config.getRep()!=null&&!config.getRep().isBlank(),"le répertoire '%s' est vide",config.getRep());
        var f = Paths.get(config.getRep(), s5);
        LOGGER.info("f is '{}'", f);
        Verify.verify(f.isAbsolute(),"fichier '%s' n'est pas absolu",f);
        if (f.getParent()!=null&&Files.notExists(f.getParent())) {
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
