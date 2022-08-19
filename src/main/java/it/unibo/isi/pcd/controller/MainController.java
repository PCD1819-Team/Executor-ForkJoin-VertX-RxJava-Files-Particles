package it.unibo.isi.pcd.controller;

import com.sun.javafx.application.PlatformImpl;

import it.unibo.isi.pcd.view.MainFxFXMLController;
import it.unibo.isi.pcd.view.MainView;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainController {

  /**
   * Controller principale, lancia il main.
   */
  public static void main(final String[] args) {
    PlatformImpl.startup(() -> {
    });
    MainView view;
    final UpperController exController;
    final MainFxFXMLController viewController;

    view = MainView.getInstance();
    viewController = view.setupViewController();
    Platform.runLater(() -> {
      try {
        final Stage primaryStage = new Stage(StageStyle.DECORATED);
        primaryStage.setTitle("Assignment2");
        view.start(primaryStage);
      } catch (final Exception e) {
        e.printStackTrace();
      }
    });
    exController = new UpperController(viewController);
    viewController.addObserver(exController);
    exController.waitEvent();
  }

}
