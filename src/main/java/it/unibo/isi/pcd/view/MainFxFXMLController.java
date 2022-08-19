package it.unibo.isi.pcd.view;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import it.unibo.isi.pcd.model.TaskResult.ResultType;
import it.unibo.isi.pcd.utils.EmptyEvent;
import it.unibo.isi.pcd.utils.Event;
import it.unibo.isi.pcd.utils.FileEvent;
import it.unibo.isi.pcd.utils.Observable;
import it.unibo.isi.pcd.utils.Observer;
import it.unibo.isi.pcd.utils.StartEvent;
import it.unibo.isi.pcd.utils.StartEvent.EsType;
import it.unibo.isi.pcd.utils.SystemUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainFxFXMLController implements Initializable, Observable {

  private static final int MAX_LENGTH_STRING = 2;
  private final List<Control> controls = new ArrayList<>();
  private final Set<Observer> observers = new HashSet<>();
  /* Stages */
  private Stage primaryStage;

  /* AnchorPane */
  @FXML
  private AnchorPane anPane;

  /* TextArea */
  @FXML
  private TextArea mainTextArea;

  /* SplitMenuButton */
  @FXML
  private ListView<String> filesList;

  /* TextField */
  @FXML
  private TextField textPath;

  @FXML
  private TextField textWordNumber;

  /* Buttons */
  @FXML
  private Button startStopBtn;

  @FXML
  private Button addFileBtn;

  @FXML
  private Button removeFileBtn;

  @FXML
  private Button changeFilePathBtn;

  /* Buttons */

  @FXML
  private Slider threadSlider;

  /* Radio */
  @FXML
  private RadioButton radio1;

  @FXML
  private RadioButton radio2;

  @FXML
  private RadioButton radio3;

  /* --------------------------Actions --------------------------- */

  @FXML
  private void startStopAction() {
    if (this.textWordNumber.getText().length() > 0) {
      if (Files.isDirectory(Paths.get(this.textPath.getText()))) {

        this.textPath.setBackground(new Background(
            new BackgroundFill(Color.web("#FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));
        this.textWordNumber.setBackground(new Background(
            new BackgroundFill(Color.web("#FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));
        this.toggleAll();
        if (this.startStopBtn.getText().equals("Start")) {
          final StartEvent.EsType es;
          if (this.radio1.isSelected()) {
            es = EsType.ES1;
          } else if (this.radio2.isSelected()) {
            es = EsType.ES2;
          } else {
            es = EsType.ES3;
          }
          this.observers.forEach(obs -> {
            obs.notifyEvent(new StartEvent((int) this.threadSlider.getValue(), es,
                Integer.parseInt(this.textWordNumber.getText()),
                new File(this.textPath.getText())));
          });
        } else {
          this.observers.forEach(obs -> {
            obs.notifyEvent(new EmptyEvent(Event.EventType.STOP));
          });
        }
        this.toggleOnOffBtn();
      } else {
        this.textPath.setBackground(new Background(
            new BackgroundFill(Color.web("#F44242"), CornerRadii.EMPTY, Insets.EMPTY)));
      }
    } else {
      this.textWordNumber.setBackground(new Background(
          new BackgroundFill(Color.web("#F44242"), CornerRadii.EMPTY, Insets.EMPTY)));
    }
  }

  @FXML
  private void addFileAction() {
    final File choosenfile = this.openFileChooser();
    if (choosenfile != null) {
      final Path path = choosenfile.toPath();
      this.observers.forEach(obs -> {
        obs.notifyEvent(new FileEvent(path, Event.EventType.ADD_FILE, ResultType.ADD));
      });
    }
  }

  @FXML
  private void removeFileAction() {
    final String path = this.filesList.getSelectionModel().getSelectedItem();
    if (path != null) {
      this.filesList.getItems().remove(path);
      this.observers.forEach(obs -> {
        obs.notifyEvent(
            new FileEvent(Paths.get(path), Event.EventType.REMOVE_FILE, ResultType.REMOVE));
      });
    }
  }

  @FXML
  private void changePathAction() {
    final File selectedDirectory = this.openDirectoryChooser();
    if ((selectedDirectory != null)) {
      this.textPath.setText(selectedDirectory.getPath());
    }
  }

  /*------------------ Metods ----------------------------*/

  private File openFileChooser() {
    final FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select one file to import in actual folder");
    fileChooser.setInitialDirectory(new File(SystemUtils.getHomePath()));
    final File choosenFile = fileChooser.showOpenDialog(this.primaryStage);
    return choosenFile;
  }

  private File openDirectoryChooser() {
    final DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Select one folder");
    chooser.setInitialDirectory(new File(SystemUtils.getHomePath()));
    final File selectedDirectory = chooser.showDialog(this.primaryStage);
    return selectedDirectory;
  }

  public MainFxFXMLController() {
  }

  private void toggleOnOffBtn() {
    this.startStopBtn.setText(this.startStopBtn.getText().equals("Start") ? "Stop" : "Start");
  }

  private void toggleAddBtn() {
    this.addFileBtn.setDisable(!this.addFileBtn.isDisabled());
    this.removeFileBtn.setDisable(!this.removeFileBtn.isDisabled());
  }

  private void toggleAll() {
    this.controls.forEach(control -> {
      control.setDisable(!control.isDisable());
    });
  }

  private void initSliders() {
    this.threadSlider.setMin(1);
    this.threadSlider.setMax(SystemUtils.getCores());
    this.threadSlider.setBlockIncrement(1);
    this.threadSlider.setMajorTickUnit(1);
    this.threadSlider.setMinorTickCount(0);
    this.threadSlider.setShowTickLabels(true);
    this.threadSlider.setSnapToTicks(true);
  }

  private void initTxtareas() {
    this.mainTextArea.setEditable(false);
  }

  private void commonInit() {
    this.controls.addAll(
        Arrays.asList(this.radio1, this.radio2, this.radio3, this.textPath, this.changeFilePathBtn,
            this.textWordNumber, this.removeFileBtn, this.addFileBtn, this.threadSlider));
    this.removeFileBtn.setDisable(true);
    this.addFileBtn.setDisable(true);

  }

  private void initRadio() {
    this.radio1.setSelected(true);
  }

  private void initListView() {

  }

  private void initTextFields() {
    this.textPath.setText(SystemUtils.getHomePath());
    this.textPath.setCache(true);
    this.textWordNumber.textProperty()
        .addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
          if (!newValue.matches("\\d*") || (this.textWordNumber.getText()
              .length() > MainFxFXMLController.MAX_LENGTH_STRING)) {
            this.textWordNumber.setText(oldValue);
          }
        });
    this.textWordNumber.setText("5");
  }

  @Override
  @FXML
  public void initialize(final URL location, final ResourceBundle resources) {
    this.initSliders();
    this.initTxtareas();
    this.initRadio();
    this.initTextFields();
    this.commonInit();
    this.initListView();
  }

  @Override
  public void addObserver(final Observer obs) {
    this.observers.add(obs);
  }

  public void update(final List<Entry<String, Integer>> result) {
    if (!this.isViewThread()) {
      Platform.runLater(() -> {
        this.internalUpdate(result);
      });
    } else {
      this.internalUpdate(result);
    }

  }

  private void internalUpdate(final List<Entry<String, Integer>> result) {
    this.mainTextArea.clear();
    result.forEach(e -> {
      this.mainTextArea.setText(this.mainTextArea.getText() + "\n" + "Parola:: " + e.getKey()
          + " | Valore:: " + e.getValue());
    });

  }

  private boolean isViewThread() {
    return Platform.isFxApplicationThread();
  }

  public void updateFiles(final Path result, final boolean ifAdd) {
    if (!this.isViewThread()) {
      Platform.runLater(() -> {
        this.internalupdateFiles(result, ifAdd);
      });
    } else {
      this.internalupdateFiles(result, ifAdd);
    }
  }

  private void internalupdateFiles(final Path result, final boolean ifAdd) {

    if (ifAdd) {
      this.filesList.getItems().add(result.toString());
    } else {
      this.filesList.getItems().remove(result.toString());

    }

  }

  @Override
  public void removeObserver(final Observer obs) {
    this.observers.remove(obs);
  }

  public void setStage(final Stage passedstage) {
    this.primaryStage = passedstage;
    this.primaryStage.setOnCloseRequest(x -> {
      this.observers.forEach(obs -> {
        obs.notifyEvent(new EmptyEvent(Event.EventType.SHUTDOWN));
      });
    });
  }

  public void clear() {
    if (!this.isViewThread()) {
      Platform.runLater(() -> {
        this.mainTextArea.clear();
        this.filesList.getItems().clear();
      });
    } else {
      this.mainTextArea.clear();
      this.filesList.getItems().clear();
    }
  }

  public void enableAll() {
    if (!this.isViewThread()) {
      Platform.runLater(() -> {
        this.toggleAddBtn();
        this.toggleAll();
        this.toggleOnOffBtn();
      });
    } else {
      this.toggleAddBtn();
      this.toggleAll();
      this.toggleOnOffBtn();
    }

  }

  public void showErrorDialog(final String str) {
    if (!this.isViewThread()) {
      Platform.runLater(() -> {
        this.internalShowErrorDialog(str);
      });
    } else {
      this.internalShowErrorDialog(str);
    }
  }

  private void internalShowErrorDialog(final String str) {
    final Alert alert = new Alert(AlertType.ERROR, str, ButtonType.OK);
    alert.show();
  }

  public void addFileToList(final Path path) {
    if (!this.isViewThread()) {
      Platform.runLater(() -> {
        this.internalAddFileToList(path);
      });
    } else {
      this.internalAddFileToList(path);
    }
  }

  public void internalAddFileToList(final Path path) {
    this.filesList.getItems().add(path.toString());
  }

  public static void shutdown() {
    com.sun.javafx.application.PlatformImpl.tkExit();
    Platform.exit();
  }

}
