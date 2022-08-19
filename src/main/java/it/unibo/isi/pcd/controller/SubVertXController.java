package it.unibo.isi.pcd.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystem;
import it.unibo.isi.pcd.model.ModelMaster;
import it.unibo.isi.pcd.model.ModelStorage;
import it.unibo.isi.pcd.model.TaskResult;
import it.unibo.isi.pcd.model.TaskResult.ResultType;
import it.unibo.isi.pcd.model.WorkerVerticle;
import it.unibo.isi.pcd.utils.EmptyEvent;
import it.unibo.isi.pcd.utils.Event;
import it.unibo.isi.pcd.utils.FileEvent;
import it.unibo.isi.pcd.utils.ObservableQueue;
import it.unibo.isi.pcd.utils.StartEvent;
import it.unibo.isi.pcd.view.MainFxFXMLController;

/**
 * Sotto-controller per il secondo esercizio.
 */
public class SubVertXController implements SubController {
  public static final String ADDRESS = "Controller.Sub";
  private ModelMaster mainModel;
  private final ModelStorage modS;
  private final MainFxFXMLController view;
  private Vertx vertx;
  private final ObservableQueue<Event> eventQueue;
  private MainVerticle mainVerticle;

  public SubVertXController(final MainFxFXMLController view, final ObservableQueue<Event> queue,
      final int numWord) {
    this.vertx = null;
    this.mainModel = null;
    this.eventQueue = queue;
    this.view = view;
    this.modS = new ModelStorage(numWord);
  }

  @Override
  public void stopAction() {
    try {
      this.mainVerticle.stop();
    } catch (final Exception e) {
      e.printStackTrace();
    }
    this.vertx.close();
  }

  @Override
  public void addFileAction(final FileEvent ev) {
    this.mainVerticle.addDocument(ev);
  }

  @Override
  public void removeFileAction(final FileEvent ev) {
    this.mainVerticle.removeDocument(ev);
  }

  @Override
  public void shutdownAction() {
    this.stopAction();
  }

  @Override
  public void startAction(final StartEvent ev) {
    this.mainModel = new ModelMaster(ev.getWordLength());
    this.vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(1));
    this.mainVerticle = new MainVerticle(ev.getStartDir(), ev.getNumThreads(), ev.getWordLength(),
        this.eventQueue);
    this.vertx.deployVerticle(this.mainVerticle);
  }

  class MainVerticle extends AbstractVerticle {
    private final String startDir;
    private final int thdNumb;
    private final int wordLen;
    private FileSystem vertxFileS;
    private WorkerVerticle workerVerticle;
    private int counter;
    private final ObservableQueue<Event> queue;

    public MainVerticle(final File startDir, final int thdNumb, final int wordLen,
        final ObservableQueue<Event> queue) {
      this.startDir = startDir.toString();
      this.thdNumb = thdNumb;
      this.wordLen = wordLen;
      this.counter = 0;
      this.queue = queue;
    }

    private void handleSequence(final TaskResult result, final boolean ifAdd) {
      if (ifAdd) {
        SubVertXController.this.modS.addDocument(result.getPath());
      } else {
        SubVertXController.this.modS.removeDocument(result.getPath());
      }
      SubVertXController.this.view.update(SubVertXController.this.mainModel.computeFile(result,
          ifAdd, SubVertXController.this.modS));
      SubVertXController.this.view.updateFiles(result.getPath(), ifAdd);
    }

    @Override
    public void start(final Future<Void> future) {

      this.vertxFileS = this.vertx.fileSystem();
      final DeploymentOptions options = new DeploymentOptions().setWorker(true);

      this.workerVerticle = new WorkerVerticle(this.vertxFileS, this.wordLen, this.thdNumb);

      this.vertx.deployVerticle(this.workerVerticle, options, deploResult -> {
        this.workerVerticle.computeFolder(this.startDir).setHandler(result -> {
          result.result().size();
          this.elaborateAllFile(result.result());
        });
      });
    }

    private void elaborateAllFile(final List<String> strLis) {
      if ((strLis != null) && !strLis.isEmpty()) {
        final List<String> lis = new ArrayList<>(strLis);
        this.counter = lis.size();
        lis.forEach(txtFile -> {
          this.elaborateDocument(txtFile, ResultType.STD);
        });
      }
    }

    private void elaborateDocument(final String path, final ResultType type) {
      this.workerVerticle.computeTxtFile(path, type).setHandler(txtResult -> {
        if (txtResult.result() != null) {
          this.handleSequence(txtResult.result(), true);

        }
        if (--this.counter <= 0) {
          this.queue.offer(new EmptyEvent(Event.EventType.CONCLUDED));
        }
      });
    }

    public void addDocument(final FileEvent ev) {
      this.singleDocumentAction(ev);
    }

    public void removeDocument(final FileEvent ev) {
      this.singleDocumentAction(ev);
    }

    private void singleDocumentAction(final FileEvent ev) {
      this.vertx.runOnContext(call -> {
        final ResultType type = ev.getSubType();
        this.workerVerticle.computeTxtFile(ev.getContent().toString(), type).setHandler(result -> {
          if (result.result() != null) {
            this.handleSequence(result.result(), false);
          }
        });
      });
    }

    @Override
    public void stop() throws Exception {
      this.workerVerticle.stop();
    }
  }

}
