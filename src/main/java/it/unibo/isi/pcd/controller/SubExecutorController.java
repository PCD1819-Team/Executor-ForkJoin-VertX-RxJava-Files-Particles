package it.unibo.isi.pcd.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import it.unibo.isi.pcd.model.FolderSearchTask;
import it.unibo.isi.pcd.model.ModelMaster;
import it.unibo.isi.pcd.model.ModelStorage;
import it.unibo.isi.pcd.model.ReadFileTask;
import it.unibo.isi.pcd.model.TaskResult;
import it.unibo.isi.pcd.model.TaskResult.ResultType;
import it.unibo.isi.pcd.utils.EmptyEvent;
import it.unibo.isi.pcd.utils.Event;
import it.unibo.isi.pcd.utils.FileEvent;
import it.unibo.isi.pcd.utils.ObservableQueue;
import it.unibo.isi.pcd.utils.StartEvent;
import it.unibo.isi.pcd.utils.SystemUtils;
import it.unibo.isi.pcd.view.MainFxFXMLController;

/**
 * Sotto-controller per il primo esercizio.
 */
public class SubExecutorController implements SubController {

  private ForkJoinPool forkJoinPool;
  private final ExecutorService workerThread;
  private ModelMaster mainModel;
  private final ModelStorage modS;
  private final ObservableQueue<Event> queue;
  private final List<Future<?>> stoppableFutures;
  private ThreadPoolExecutor exec;
  private ExecutorCompletionService<TaskResult> ecs;
  private final AtomicInteger counter;
  private final AtomicBoolean scanEnd;
  private Future<?> fut;
  private int numword;
  private final MainFxFXMLController view;

  public SubExecutorController(final int numTopWords, final ObservableQueue<Event> passedQueue,
      final MainFxFXMLController view) {
    this.mainModel = null;
    this.modS = new ModelStorage(numTopWords);
    this.workerThread = Executors.newSingleThreadExecutor();
    this.forkJoinPool = new ForkJoinPool();
    this.queue = passedQueue;
    this.stoppableFutures = new ArrayList<>();
    this.numword = 0;
    this.fut = null;
    this.counter = new AtomicInteger(0);
    this.view = view;
    this.scanEnd = new AtomicBoolean(false);
  }

  @Override
  public void stopAction() {

    if (this.forkJoinPool != null) {

      this.forkJoinPool.shutdownNow();
    }
    if (this.exec != null) {

      this.exec.shutdownNow();
    }

    if (this.workerThread != null) {
      this.workerThread.shutdownNow();
    }
    this.stoppableFutures.forEach(future -> {
      future.cancel(true);
    });

  }

  @Override
  public void addFileAction(final FileEvent ev) {
    if (SystemUtils.isTxtFile(ev.getContent()) && SystemUtils.isValidFile(ev.getContent())) {
      this.counter.addAndGet(1);
      this.ecs.submit(new ReadFileTask(this.numword, ev.getContent().toFile(), ResultType.ADD));
    } else {
      this.view.showErrorDialog("File non valido");
    }
  }

  @Override
  public void removeFileAction(final FileEvent ev) {
    if (SystemUtils.isTxtFile(ev.getContent()) && SystemUtils.isValidFile(ev.getContent())) {
      this.ecs.submit(new ReadFileTask(this.numword, ev.getContent().toFile(), ResultType.REMOVE));
    } else {
      this.view.showErrorDialog("File non valido");
    }
    this.counter.addAndGet(1);
  }

  @Override
  public void shutdownAction() {
    this.stopAction();
    try {
      Thread.sleep(500);
    } catch (final InterruptedException e) {
    }
    MainFxFXMLController.shutdown();
  }

  private void asincGetTaskNumber(final ForkJoinTask<Integer> task) {
    final Thread th = (new Thread() {
      @Override
      public void run() {
        try {
          SubExecutorController.this.counter.addAndGet(task.get());
          SubExecutorController.this.scanEnd.set(true);
          SubExecutorController.this.fut.cancel(true);
        } catch (final ExecutionException | CancellationException | InterruptedException e1) {
        }
      }
    });
    th.setName("Recursive_Thread_Launcher");
    th.start();
  }

  private void startComputation(final StartEvent ev) {
    final FolderSearchTask folderSearchTask = new FolderSearchTask(ev.getStartDir(),
        SubExecutorController.this.ecs, this.numword);
    final ForkJoinTask<Integer> recursiveTask = this.forkJoinPool.submit(folderSearchTask);
    this.asincGetTaskNumber(recursiveTask);
  }

  private void update(final TaskResult res, final boolean ifAdd) {
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

    this.fut = this.workerThread.submit((() -> {
      this.exec = (ThreadPoolExecutor) Executors.newFixedThreadPool(ev.getNumThreads());
      this.ecs = new ExecutorCompletionService<>(this.exec);
      this.numword = ev.getWordLength();
      this.forkJoinPool = new ForkJoinPool(ev.getNumThreads());
      this.mainModel = new ModelMaster(ev.getWordLength());

      this.startComputation(ev);

      for (int i = 0; ((!this.scanEnd.get()) || (i < this.counter.get())); i++) {
        try {
          final Future<TaskResult> futureResult = this.ecs.take();
          if (futureResult.isDone() && (futureResult.get() != null)) {
            final TaskResult.ResultType type = futureResult.get().getType();

            switch (type) {
              case STD:

              case ADD:
                this.update(futureResult.get(), true);

                break;

              case REMOVE:
                this.update(futureResult.get(), false);

                break;

              default:
                break;
            }
          }
        } catch (final ExecutionException | InterruptedException e) {
          if ((this.exec.getActiveCount() > 0) || ((i - 1) < this.counter.get())) {
            i--;
            continue;
          } else {
            break;
          }
        }
      }
      if (!this.exec.isShutdown()) {
        this.queue.offer(new EmptyEvent(Event.EventType.CONCLUDED));
        this.stopAction();
      }
    }));
    this.stoppableFutures.add(this.fut);
  }

}
