package it.unibo.isi.pcd.utils;

import java.nio.file.Path;

import it.unibo.isi.pcd.model.TaskResult.ResultType;

public class FileEvent implements Event {

  private final EventType evType;
  private final Path file;
  private final ResultType subType;

  /*
   * .
   */
  public FileEvent(final Path savefile, final EventType evType, final ResultType subType) {
    this.evType = evType;
    this.file = savefile;
    this.subType = subType;
  }

  @Override
  public EventType getType() {

    return this.evType;
  }

  public ResultType getSubType() {

    return this.subType;
  }

  public Path getContent() {
    return this.file;
  }

}
