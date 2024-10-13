package com.example.starter;

import java.io.Serializable;
import java.util.List;

public class ListFiles2 implements Serializable {
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

  @Override
  public String toString() {
    return "ListFiles2{" +
      "liste=" + liste +
      ", code='" + code + '\'' +
      '}';
  }
}
