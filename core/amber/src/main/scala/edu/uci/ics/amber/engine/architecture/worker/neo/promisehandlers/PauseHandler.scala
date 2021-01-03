package edu.uci.ics.amber.engine.architecture.worker.neo.promisehandlers

import akka.actor.ActorContext
import edu.uci.ics.amber.engine.architecture.worker.WorkerState
import edu.uci.ics.amber.engine.architecture.worker.neo.WorkerInternalQueue.DummyInput
import edu.uci.ics.amber.engine.architecture.worker.neo.WorkerPromiseManager
import edu.uci.ics.amber.engine.architecture.worker.neo.promisehandlers.PauseHandler.WorkerPause
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.{ExecutionPaused, ReportState}
import edu.uci.ics.amber.engine.common.promise.{
  PromiseBody,
  PromiseCompleted,
  PromiseHandler,
  SynchronizedInvocation
}

object PauseHandler {
  final case class WorkerPause() extends PromiseBody[PromiseCompleted]
}

trait PauseHandler extends PromiseHandler {
  this: WorkerPromiseManager =>

  registerHandler {
    case WorkerPause() =>
      val p = createLocalPromise[ExecutionPaused]()
      pauseManager.registerPromise(p)
      pauseManager.pause()
      // if dp thread is blocking on waiting for input tuples:
      if (dataProcessor.isQueueEmpty) {
        // insert dummy batch to unblock dp thread
        dataProcessor.appendElement(DummyInput())
      }
      p.map { res =>
        returning()
      }
  }
}