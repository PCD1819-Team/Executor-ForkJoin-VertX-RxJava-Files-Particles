package it.unibo.isi.pcd.controller;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import it.unibo.isi.pcd.model.FileAnalyzer;
import it.unibo.isi.pcd.model.ModelMaster;
import it.unibo.isi.pcd.model.ModelStorage;
import it.unibo.isi.pcd.model.TaskResult;
import it.unibo.isi.pcd.model.TaskResult.ResultType;
import it.unibo.isi.pcd.utils.Event;
import it.unibo.isi.pcd.utils.FileEvent;
import it.unibo.isi.pcd.utils.ObservableQueue;
import it.unibo.isi.pcd.utils.StartEvent;
import it.unibo.isi.pcd.utils.SystemUtils;
import it.unibo.isi.pcd.view.MainFxFXMLController;

/**
 * Sotto-controller per il terzo esercizio.
 */
public class SubRxJavaController implements SubController {
  ExecutorService exec;
  Scheduler sched;
  private ModelMaster mainModel;
  private final ModelStorage modS;
  private final MainFxFXMLController view;
  private final ObservableQueue<Event> eventQueue;
  private int numWord;

  public SubRxJavaController(final MainFxFXMLController view, final ObservableQueue<Event> queue,
      final int numWord) {
    this.sched = null;
    this.exec = null;
    this.numWord = 0;
    this.mainModel = null;
    this.eventQueue = queue;
    this.view = view;
    this.modS = new ModelStorage(numWord);
  }

  @Override
  public void stopAction() {
    this.exec.shutdownNow();
    this.sched.shutdown();
  }

  private Observable<Observable<Path>> serchTxtFiles(final Path dir) {

    return Observable.fromCallable(() -> this.elaborateFolder(dir))
        .onErrorReturnItem(Observable.empty());
  }

  private Observable<Path> elaborateFolder(final Path dir) throws IOException {

    try (final DirectoryStream<Path> children = Files.newDirectoryStream(dir)) {
      final List<Path> subfolders = Observable.fromIterable(children).toList().blockingGet();

      return Observable.fromIterable(subfolders).observeOn(this.sched).filter(file -> {
        if (SystemUtils.isValidFile(file)
            && (Files.isDirectory(file) || SystemUtils.isTxtFile(file))) {
          return true;
        }
        return false;
      }).flatMap(
          path -> (!Files.isDirectory(path) ? Observable.just(path)
              : this.serchTxtFiles(path).blockingSingle()),
          Runtime.getRuntime().availableProcessors());
    }

  }

  private Observable<TaskResult> elaborateSingleFile(final Path dir) {
    final Observable<TaskResult> source = Observable.create(emitter -> {
      final List<String> lines = new ArrayList<>();
      Map<String, Integer> map = new HashMap<>();
      FileAnalyzer fileAn;
      try {
        lines.addAll(new ArrayList<>(Files.readAllLines(dir)));
        fileAn = new FileAnalyzer(this.numWord, lines);
        map = fileAn.calculate();
      } catch (final IOException e) {
      }

      if (map.isEmpty()) {
        emitter.onNext(new TaskResult(ResultType.ADD, null, null));
      } else {
        emitter.onNext(new TaskResult(ResultType.ADD, map, dir));
      }
    });
    return source;
  }

  @Override
  public void addFileAction(final FileEvent ev) {
    this.elaborateSingleFile(ev.getContent()).observeOn(this.sched).subscribe(result -> {
      this.update(result, true);
    });
  }

  @Override
  public void removeFileAction(final FileEvent ev) {
    this.elaborateSingleFile(ev.getContent()).observeOn(this.sched).subscribe(result -> {
      this.update(result, false);
    });
  }

  @Override
  public void shutdownAction() {
    this.exec.shutdownNow();
    this.sched.shutdown();
  }

  private Observable<TaskResult> analizeFile(final Path dir) {
    final Observable<TaskResult> source = Observable.create(emitter -> {
      final FileAnalyzer analizer;
      Map<String, Integer> map = null;
      final List<String> lines = new ArrayList<>();
      try {
        lines.addAll(new ArrayList<>(Files.readAllLines(dir)));
      } catch (final IOException e) {
      }

      if (!lines.isEmpty()) {
        analizer = new FileAnalyzer(this.numWord, lines);
        map = analizer.calculate();
      }

      if ((map != null) && !map.isEmpty()) {
        emitter.onNext(new TaskResult(ResultType.STD, map, dir));
      } else {
        emitter.onNext(new TaskResult(ResultType.STD, null, null));
      }
      emitter.onComplete();

    });
    return source;
  }

  private synchronized void update(final TaskResult res, final boolean ifAdd) {
    if ((res.getContent() != null) && !res.getContent().isEmpty()) {
      if (ifAdd) {
        this.modS.addDocument(res.getPath());
      } else {
        this.modS.removeDocument(res.getPath());
      }
      this.view.update(this.mainModel.computeFile(res, ifAdd, this.modS));
      this.view.updateFiles(res.getPath(), ifAdd);
    }
  }

  @Override
  public void startAction(final StartEvent ev) {

    this.exec = Executors.newFixedThreadPool(ev.getNumThreads());
    this.sched = Schedulers.from(this.exec, false);
    this.numWord = ev.getWordLength();
    this.mainModel = new ModelMaster(this.numWord);
    this.serchTxtFiles(ev.getStartDir().toPath()).subscribe(folderObs -> {
      folderObs.subscribeOn(this.sched).subscribe(txtPath -> {
        this.analizeFile(txtPath).subscribe(result -> {
          this.update(result, true);
        });
      });
    });

  }
}
