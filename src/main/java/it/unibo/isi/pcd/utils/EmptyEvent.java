package it.unibo.isi.pcd.utils;

public class EmptyEvent implements Event {

  private final EventType type;

  /*
   * .
   */
  public EmptyEvent(final EventType ev) {
    this.type = ev;

  }

  @Override
  public EventType getType() {

    return this.type;
  }

}
