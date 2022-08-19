package it.unibo.isi.pcd.utils;

import java.nio.file.Path;
import java.util.List;

public class Document {

  private final List<String> lines;
  private final Path path;

  public Document(final List<String> lines, final Path path) {
    this.lines = lines;
    this.path = path;
  }

  public List<String> getLines() {
    return this.lines;
  }

  public Path getPath() {
    return this.path;
  }

}
