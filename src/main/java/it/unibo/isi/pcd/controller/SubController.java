package it.unibo.isi.pcd.controller;

import it.unibo.isi.pcd.utils.FileEvent;
import it.unibo.isi.pcd.utils.StartEvent;

/**
 * Interfaccia implementata da tutti i sottocontroller, per ogni esercizio.
 */
public interface SubController {

  /**
   * Blocca l'esecuzione.
   */
  void stopAction();

  /**
   * Aggiunge un file da elaborare.
   */
  void addFileAction(final FileEvent ev);

  /**
   * Rimuove un file elaborato.
   */
  void removeFileAction(final FileEvent ev);

  /**
   * Blocca e esce.
   */
  void shutdownAction();

  /**
   * Inizia la procedura di ricerca e elaborazione.
   */
  void startAction(final StartEvent ev);
}
