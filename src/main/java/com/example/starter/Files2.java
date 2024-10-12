package com.example.starter;

public class Files2 {
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

  @Override
  public String toString() {
    return "Files2{" +
      "filename='" + filename + '\'' +
      ", size=" + size +
      ", hash='" + hash + '\'' +
      ", type='" + type + '\'' +
      '}';
  }
}
