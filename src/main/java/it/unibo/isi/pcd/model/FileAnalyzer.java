package it.unibo.isi.pcd.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileAnalyzer {

  private final Map<String, Integer> map;
  private final int wordsMinLength;
  private final List<String> lines;

  public FileAnalyzer(final int wordsMinLength, final List<String> lines) {
    this.wordsMinLength = wordsMinLength;
    this.map = new HashMap<>();
    this.lines = lines;
  }

  private void addOccurence(String word) {
    if (word.length() >= this.wordsMinLength) {
      word = word.toLowerCase();
      this.map.merge(word, 1, Integer::sum);

    }

  }

  public Map<String, Integer> calculate() {

    String[] words;

    for (int x = 0; x < this.lines.size(); x++) {
      words = this.lines.get(x).split("\\P{L}+");
      for (int i = 0; i < words.length; i++) {
        this.addOccurence(words[i]);
      }
    }
    return this.map;
  }

}
