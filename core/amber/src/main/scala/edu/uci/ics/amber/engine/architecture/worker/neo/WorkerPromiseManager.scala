package edu.uci.ics.amber.engine.architecture.worker.neo

import com.twitter.util.Promise
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlOutputPort
import edu.uci.ics.amber.engine.architecture.worker.neo.promisehandlers.PauseHandler
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.ExecutionPaused
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.promise.{PromiseInvocation, PromiseManager, WorkflowPromise}
import edu.uci.ics.amber.engine.common.statetransition.WorkerStateManager

class WorkerPromiseManager(
    val selfID: ActorVirtualIdentity,
    val controlOutputPort: ControlOutputPort,
    val pauseManager: PauseManager,
    val dataProcessor: DataProcessor,
    val workerStateManager: WorkerStateManager
) extends PromiseManager(selfID, controlOutputPort)
    with PauseHandler {

  def createLocalPromise[T](): WorkflowPromise[T] = {
    val ctx = mkPromiseContext()
    promiseID += 1
    val promise = WorkflowPromise[T](promiseContext)
    unCompletedPromises(ctx) = promise
    promise
  }

}
