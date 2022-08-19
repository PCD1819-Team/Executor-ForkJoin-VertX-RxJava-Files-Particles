package it.unibo.isi.pcd.controller;

import java.io.File;

import it.unibo.isi.pcd.utils.Event;
import it.unibo.isi.pcd.utils.FileEvent;
import it.unibo.isi.pcd.utils.ObservableQueue;
import it.unibo.isi.pcd.utils.Observer;
import it.unibo.isi.pcd.utils.StartEvent;
import it.unibo.isi.pcd.utils.StartEvent.EsType;
import it.unibo.isi.pcd.view.MainFxFXMLController;

/**
 * Controller che fa da intermediario tra i sotto-controller e la view, qui il
 * Main Thread gestisce gli eventi.
 */
public class UpperController implements Observer {

  private final MainFxFXMLController view;
  private final ObservableQueue<Event> queue;
  private static int numTopWords = 10;
  private SubController chosenController;

  public UpperController(final MainFxFXMLController passview) {
    this.view = passview;
    this.queue = new ObservableQueue<>();
    this.chosenController = null;
  }

  /**
   * Carica il sotto-controller selezionato.
   */
  private void loadSubController(final EsType esType) {

    switch (esType) {
      case ES1:
        this.chosenController = new SubExecutorController(UpperController.numTopWords, this.queue,
            this.view);
        break;

      case ES2:
        this.chosenController = new SubVertXController(this.view, this.queue,
            UpperController.numTopWords);
        break;

      case ES3:
        this.chosenController = new SubRxJavaController(this.view, this.queue,
            UpperController.numTopWords);
        break;

      default:
        break;
    }

  }

  /**
   * Il main Thread si mette in attesa degli eventi da gestire.
   */
  public void waitEvent() {
    boolean exit = false;
    while (!exit) {
      final Event ev = this.queue.poll();
      switch (ev.getType()) {

        case ADD_FILE:
          this.chosenController.addFileAction((FileEvent) ev);
          ;
          break;

        case REMOVE_FILE:
          this.chosenController.removeFileAction((FileEvent) ev);
          break;

        case START:
          final StartEvent sEv = (StartEvent) ev;
          this.loadSubController(sEv.getEsType());
          this.chosenController.startAction(sEv);
          this.view.clear();
          break;

        case STOP:
          this.chosenController.stopAction();
          break;

        case CONCLUDED:
          this.view.enableAll();
          break;

        case SHUTDOWN:
          if (this.chosenController != null) {
            this.chosenController.shutdownAction();
          }
          exit = true;
          break;

        default:
          break;
      }
    }
  }

  @Override
  public void notifyEvent(final Event ev) {
    this.queue.offer(ev);
  }

  public void scheduleFile(final File scheduledfile) {

  }

}
