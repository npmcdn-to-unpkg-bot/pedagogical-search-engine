package agents.helpers

import java.io.File
import java.util.Calendar
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import utils.{Files, Logger, Settings}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class FileExplorer(jobName: String = Calendar.getInstance.getTime.toString,
                   nbTasks: Int = Runtime.getRuntime.availableProcessors(),
                   settings: Settings = new Settings()
                  ) {
  
  def launch(process: (File, ExecutionContext) => Future[Any])
  : Unit = {

    val start = System.nanoTime()

    // Create the queues
    val utilsExec = Executors.newFixedThreadPool(10)
    val utilsQueue = ExecutionContext.fromExecutor(utilsExec)

    val tasksExec = Executors.newFixedThreadPool(nbTasks)
    val tasksQueue = ExecutionContext.fromExecutor(tasksExec)

    val workingExec = Executors.newFixedThreadPool(nbTasks * 10) // should be large enough
    val workingQueue = ExecutionContext.fromExecutor(workingExec)

    // Create a monitoring system
    var shutDownMonitoring = false

    val totCounter = new AtomicInteger(0)

    val monitoring = Future {
      val waitedMs = new AtomicInteger(0)

      while(!shutDownMonitoring) {
        val sleepMs = 1 * 1000
        Thread.sleep(sleepMs)
        val waited = waitedMs.addAndGet(sleepMs)

        if(waited > 60 * 1000) {
          waitedMs.getAndSet(0)
          val tot = totCounter.get()

          // Log the stats
          Logger.info(s"Monitoring(rsc/min): tot=$tot")
        }
      }
      Logger.info("Monitoring was shut down")
      true
    }(utilsQueue)


    var lastTask: Future[Any] = Future.successful()

    try {
      // Prepare the iterator over resources files
      val name = jobName.replaceAll("/", "-")
      val output = Files.touch(s"${settings.Resources.Agent.workingDir}/file-explorer-job-$name/")
      val input = new File(settings.Resources.folder)
      val it = org.apache.commons.io.FileUtils.iterateFiles(
        input,
        Array("json"),
        true
      )

      // Iterate
      Logger.info(s"Launch File-explorer: #tasks=$nbTasks, input=${input.getAbsolutePath}," +
        s" output={${output.getAbsolutePath}}")


      while(it.hasNext) {
        val file = it.next().asInstanceOf[File]
        lastTask = Future {
          /*
           * It is tricky here:
           * Each process(..) has it's own thread in the
           * task queue which frees only when the process is done.
           *
           * Why?
           * This ensures that we are only running the fixed number
           * of processes defined by the tasks queue size.
           *
           * What happen if we don't?
           * We might run a undetermined number of tasks in parallel
           * and since each process takes memory, we might run out
           * of memory.
           */
          val future = process(file, workingQueue)
          Await.result(future, Duration.Inf)
        }(tasksQueue)
      }
    } catch {
      case e: Throwable =>
        Logger.error(s"File Explorer got an error: ${e.getMessage}")
    }

    // Wait for the last task
    Await.result(lastTask, Duration.Inf)
    shutDownMonitoring = true
    Await.result(monitoring, Duration.Inf)

    // Display some more stats
    val stop = System.nanoTime()
    val elapsed = (stop - start) / (1000*1000*1000) // in seconds
    Logger.info(s"$jobName finished after $elapsed seconds")

    // shutdown
    utilsExec.shutdown()
    tasksExec.shutdown()
    workingExec.shutdown()
  }

}
