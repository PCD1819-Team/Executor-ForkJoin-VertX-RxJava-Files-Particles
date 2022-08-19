package it.unibo.isi.pcd.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class ModelStorage {
  private final Map<String, Integer> occurrences;
  private final Set<Path> docs;
  private final int numTopWords;

  public ModelStorage(final int numTopWords) {
    this.occurrences = new HashMap<>();
    this.numTopWords = numTopWords;
    this.docs = new HashSet<>();
  }

  public void clearOccurrences() {
    this.occurrences.clear();
  }

  public List<Entry<String, Integer>> updateOccurrences(final Map<String, Integer> updateMap,
      final boolean add) {

    if (add) {
      updateMap.forEach((k, v) -> this.occurrences.merge(k, v, Integer::sum));
    } else {
      updateMap.forEach((k, v) -> this.occurrences.merge(k, v, (v1, v2) -> v1 - v2));
    }

    final List<Entry<String, Integer>> lis = this.occurrences.entrySet().stream()
        .collect(Collectors.toList());
    lis.sort((e1, e2) -> {
      return Integer.compare(-e1.getValue(), -e2.getValue());
    });

    if (lis.size() > (this.numTopWords - 1)) {
      return new ArrayList<>(lis.subList(0, this.numTopWords - 1));
    } else {
      return new ArrayList<>(lis);
    }

  }

  public void addDocument(final Path doc) {
    this.docs.add(doc);
  }

  public void removeDocument(final Path doc) {
    this.docs.remove(doc);
  }

  public void addDocuments(final Set<Path> docs) {
    this.docs.addAll(docs);
  }

  public void removeDocuments(final Set<Path> docs) {
    this.docs.removeAll(docs);
  }

  public Set<Path> getPaths(final Path docs) {
    return new HashSet<>(this.docs);
  }

  public void clearPaths() {
    this.docs.clear();
  }

  public boolean isPresent(final Path docs) {
    return this.docs.contains(docs);
  }

}
