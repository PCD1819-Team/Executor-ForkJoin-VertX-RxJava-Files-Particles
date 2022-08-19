package it.unibo.isi.pcd.model;

import java.nio.file.Path;
import java.util.Map;

public class TaskResult {

  public enum ResultType {
    ADD("FILE.ADD"), REMOVE("FILE.REMOVE"), STD("FILE.STD");

    private String str;

    private ResultType(final String str) {
      this.str = str;
    }

    public String getValue() {
      return this.str;
    }

  }

  private final Map<String, Integer> content;
  private final ResultType type;
  private final Path filePath;

  public TaskResult(final ResultType passedType, final Map<String, Integer> passedContent,
      final Path path) {
    this.type = passedType;
    this.content = passedContent;
    this.filePath = path;
  }

  public Map<String, Integer> getContent() {
    return this.content;
  }

  public ResultType getType() {
    return this.type;
  }

  public Path getPath() {
    return this.filePath;
  }

}
