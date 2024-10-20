package com.example.starter;

public class Config {

  private String rep;
  private int port;

  public Config(String rep, int port) {
    this.rep = rep;
    this.port = port;
  }

  public String getRep() {
    return rep;
  }

  public void setRep(String rep) {
    this.rep = rep;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }
}
