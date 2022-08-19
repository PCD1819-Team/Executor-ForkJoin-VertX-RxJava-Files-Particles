package it.unibo.isi.pcd.utils;

public interface Observable {

  void addObserver(Observer obs);

  void removeObserver(Observer obs);

}
