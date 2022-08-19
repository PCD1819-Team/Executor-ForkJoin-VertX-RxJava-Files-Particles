package it.unibo.isi.pcd.model;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ModelMaster implements Model {

  private final int wordsLength;

  private static int maxLinesPerTask = 5000;

  public ModelMaster(final int n) {
    this.wordsLength = n;

  }

//  public Integer findDocuments(final ForkJoinPool forkJoinPool, final Path startingFolder) {
//    Integer docsFound = null;
//
//    final FolderSearchTask folderSearchTask = new FolderSearchTask(startingFolder.toFile(), null);
//    final ForkJoinTask<Integer> recursiveTask = forkJoinPool.submit(folderSearchTask);
//
//    try {
//      docsFound = recursiveTask.get();
//    } catch (final ExecutionException | InterruptedException e) {
//      forkJoinPool.shutdownNow();
//      folderSearchTask.cancel(true);
//      return Optional.empty();
//    }
//
//    System.out.println("Invocata forkjoin di ricerca");
//
//    return docsFound;
//
//  }

  public List<Entry<String, Integer>> computeFile(final TaskResult futureResult,
      final boolean ifAdd, final ModelStorage modS) {
    final Map<String, Integer> result;

    result = futureResult.getContent();
    final List<Entry<String, Integer>> topOccurrences = modS.updateOccurrences(result, ifAdd);
    return topOccurrences;

  }

}
