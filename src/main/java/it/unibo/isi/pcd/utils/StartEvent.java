package it.unibo.isi.pcd.utils;

import java.io.File;

public class StartEvent implements Event {
  private final int numThreads;

  public enum EsType {
    ES1, ES2, ES3;
  }

  private final EventType type;
  private final EsType estype;
  private final int wordLength;
  private final File startDir;
  /*
   * .
   */

  public StartEvent(final int numThreads, final EsType es, final int wlen, final File dir) {
    this.type = EventType.START;
    this.numThreads = numThreads;
    this.estype = es;
    this.startDir = dir;
    this.wordLength = wlen;
  }

  @Override
  public EventType getType() {
    return this.type;
  }

  public EsType getEsType() {
    return this.estype;
  }

  public int getNumThreads() {
    return this.numThreads;
  }

  public int getWordLength() {
    return this.wordLength;
  }

  public File getStartDir() {
    return this.startDir;
  }

}
