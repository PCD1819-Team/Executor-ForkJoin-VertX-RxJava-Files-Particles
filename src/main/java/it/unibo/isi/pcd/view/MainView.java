package it.unibo.isi.pcd.view;

import java.awt.Toolkit;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainView extends Application {
  private FXMLLoader loader;
  private AnchorPane root;
  private static MainView singleton;
  private static MainFxFXMLController viewFxmlContoller;

  private MainView() {
  }

  public static MainView getInstance() {
    synchronized (MainView.class) {
      if (MainView.singleton == null) {

        MainView.singleton = new MainView();
      }
    }
    return MainView.singleton;
  }

  public MainFxFXMLController setupViewController() {
    this.loader = new FXMLLoader(this.getClass().getResource("../file/Sample.fxml"));
    try {
      this.root = (AnchorPane) this.loader.load();
      MainView.viewFxmlContoller = (MainFxFXMLController) this.loader.getController();
    } catch (final IOException e) {
      return null;
    }
    return MainView.viewFxmlContoller;
  }

  @Override
  public void start(final Stage primaryStage) throws Exception {
    try {
      final Scene scene = new Scene(this.root,
          Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2,
          Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2);
      scene.getStylesheets()
          .add(this.getClass().getResource("../file/application.css").toExternalForm());
      MainView.viewFxmlContoller.setStage(primaryStage);

      primaryStage.setScene(scene);
      primaryStage.show();

    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

}
