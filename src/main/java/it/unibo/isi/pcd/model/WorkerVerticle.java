package it.unibo.isi.pcd.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.file.FileSystem;
import it.unibo.isi.pcd.utils.SystemUtils;

public class WorkerVerticle extends AbstractVerticle {
  private final int wordLength;
  private final FileSystem vertxFileS;
  private WorkerExecutor workerPool;
  private final int thdNumb;

  public WorkerVerticle(final FileSystem vertxFileS2, final int wordL, final int thdNumb) {
    this.wordLength = wordL;
    this.vertxFileS = vertxFileS2;
    this.thdNumb = thdNumb;
  }

  @Override
  public void start(final Future<Void> future) {
    this.workerPool = this.vertx.createSharedWorkerExecutor("Worker-Bloking-Pool", this.thdNumb);
    future.complete();
  }

  @Override
  public void stop() throws Exception {
    this.workerPool.close();
  }

  public Future<TaskResult> computeTxtFile(final String str, final TaskResult.ResultType taskType) {
    final Future<TaskResult> future = Future.future();
    this.workerPool.executeBlocking(call -> {
      final FileAnalyzer analizer;
      Map<String, Integer> map = null;
      final List<String> lines = new ArrayList<>();
      try {
        lines.addAll(new ArrayList<>(Files.readAllLines(Paths.get(str))));
      } catch (final IOException e) {
      }
      if (!lines.isEmpty()) {
        analizer = new FileAnalyzer(this.wordLength, lines);
        map = analizer.calculate();
      }
      if ((map != null) && !map.isEmpty()) {
        call.complete(new TaskResult(taskType, map, Paths.get(str)));
      } else {
        call.complete();
      }
    }, false, result -> {
      if (result.result() != null) {
        final TaskResult res = (TaskResult) result.result();
        future.complete(res);
      } else {
        future.complete();
      }

    });
    return future;
  }

  private void getFiles(final File[] files, final List<String> lis) {
    for (final File file : files) {
      if (file.isDirectory() && SystemUtils.isValidFile(file.toPath())) {
        this.getFiles(file.listFiles(), lis);
      } else {
        if (SystemUtils.isTxtFile(file)) {
          lis.add(file.toString());
        }
      }
    }
  }

  public Future<List<String>> computeFolder(final String folder) {
    final Future<List<String>> future = Future.future();
    this.workerPool.executeBlocking(call -> {

      final File[] files = new File(folder).listFiles();
      final ArrayList<String> lis = new ArrayList<>();
      this.getFiles(files, lis);

      if (!lis.isEmpty()) {
        call.complete(lis);
      } else {
        call.complete();
      }
    }, false, result -> {
      if (result.result() != null) {
        @SuppressWarnings("unchecked")
        final List<String> lis = (List<String>) result.result();
        future.complete(lis);
      } else {
        future.complete();
      }

    });
    return future;
  }

}
