package it.unibo.isi.pcd.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ReadFileTask implements Callable<TaskResult> {
  private final File fileToRead;
  private Map<String, Integer> map;
  private final int wordsMinLength;
  private final TaskResult.ResultType type;
  private FileAnalyzer fileAn;

  public ReadFileTask(final int wordsMinLength, final File file, final TaskResult.ResultType type) {
    this.wordsMinLength = wordsMinLength;
    this.map = new HashMap<>();
    this.type = type;
    this.fileToRead = file;
    this.fileAn = null;
  }

  @Override
  public TaskResult call() throws Exception {
    final List<String> lines = new ArrayList<>();
    try {
      lines.addAll(new ArrayList<>(Files.readAllLines(Paths.get(this.fileToRead.getPath()))));
      this.fileAn = new FileAnalyzer(this.wordsMinLength, lines);
      this.map = this.fileAn.calculate();

    } catch (final IOException e) {
//      e.printStackTrace();
      return null;
    }

    return lines.isEmpty() ? null : new TaskResult(this.type, this.map, this.fileToRead.toPath());

  }

}
