package it.unibo.isi.pcd.utils;

public interface Event {

  enum EventType {
    STOP, START, SHUTDOWN, CONCLUDED, ADD_FILE, REMOVE_FILE;
  }

  EventType getType();

}
