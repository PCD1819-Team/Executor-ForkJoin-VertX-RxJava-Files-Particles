package it.unibo.isi.pcd.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

import it.unibo.isi.pcd.model.TaskResult.ResultType;
import it.unibo.isi.pcd.utils.Document;
import it.unibo.isi.pcd.utils.SystemUtils;

/**
 * Task adibito alla ricerca dei file di testo, per ogni file di testo trovato
 * procede a lanciare un apposito task.
 */
public class FolderSearchTask extends RecursiveTask<Integer> {

  private static final long serialVersionUID = 1L;
  private final File rootFolder;
  private final ExecutorCompletionService<TaskResult> ecs;
  private final List<Future<TaskResult>> listFutures;
  private int counter;
  private final int wordCount;

  public FolderSearchTask(final File startfolder, final ExecutorCompletionService<TaskResult> ecs,
      final int wordCount) {
    this.rootFolder = startfolder;
    this.ecs = ecs;
    this.listFutures = new ArrayList<>();
    this.counter = 0;
    this.wordCount = wordCount;
  }

  public void test() {
    Thread.currentThread().interrupt();
  }

  @Override
  protected Integer compute() {

    final Set<Document> documents = new HashSet<>();
    final List<RecursiveTask<Integer>> forkedSearchTask = new ArrayList<>();

    try {

      final File[] arr = this.rootFolder.listFiles(subfile -> {

        if (SystemUtils.isValidFile(subfile.toPath())) {
          if (subfile.isDirectory()) {
            final File[] list = subfile.listFiles();
            if ((list != null) && (list.length > 0)) {
              final FolderSearchTask newtask = new FolderSearchTask(subfile, this.ecs,
                  this.wordCount);
              forkedSearchTask.add(newtask);
              newtask.fork();
//              System.out.println("Subfile:" + subfile.toString());
            }
          } else if (subfile.isFile() && SystemUtils.isTxtFile(subfile)) {
            return true;
          } else {
            return false;
          }
        }
        return false;
      });

      if ((arr != null) && (arr.length > 0)) {
        for (int i = 0; i < arr.length; i++) {
          this.listFutures
              .add(this.ecs.submit(new ReadFileTask(this.wordCount, arr[i], ResultType.STD)));
          this.counter++;
        }
      }

      for (int i = 0; i < forkedSearchTask.size(); i++) {
        this.counter += forkedSearchTask.get(i).get();
      }

    } catch (final ExecutionException | InterruptedException e) {
      return 0;
    }
    return this.counter;

  }
}
