package edu.uci.ics.amber.engine.architecture.worker.neo

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorRef
import com.twitter.util.Promise
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlOutputPort
import edu.uci.ics.amber.engine.architecture.worker.neo.PauseManager.{NoPause, Paused}
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.ExecutionPaused
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity
import edu.uci.ics.amber.engine.common.promise.{
  PromiseCompleted,
  PromiseContext,
  ReturnPayload,
  WorkflowPromise
}

object PauseManager {
  final val NoPause = 0
  final val Paused = 1
}

class PauseManager(controlOutputPort: ControlOutputPort) {

  // current pause privilege level
  private val pausePrivilegeLevel = new AtomicInteger(PauseManager.NoPause)
  // yielded control of the dp thread
  // volatile is necessary otherwise main thread cannot notice the change.
  // volatile means read/writes are through memory rather than CPU cache
  @volatile private var dpThreadBlocker: CompletableFuture[Void] = _
  @volatile private var promiseFromActorThread: WorkflowPromise[ExecutionPaused] = _

  /** pause functionality
    * both dp thread and actor can call this function
    * @param
    */
  def pause(): Unit = {

    /*this line atomically applies the following logic:
      Level = Paused
      if(level >= pausePrivilegeLevel.get())
        pausePrivilegeLevel.set(level)
     */
    pausePrivilegeLevel.getAndUpdate { i =>
      if (Paused >= i) {
        Paused
      } else i
    }

  }

  def registerPromise(workflowPromise: WorkflowPromise[ExecutionPaused]): Unit = {
    if (isPaused) {
      controlOutputPort.sendTo(
        VirtualIdentity.Self,
        ReturnPayload(workflowPromise.ctx, ExecutionPaused())
      )
    } else {
      promiseFromActorThread = workflowPromise
    }
  }

  def isPaused: Boolean = isPauseSet() && dpThreadBlocker != null

  /** resume functionality
    * only actor calls this function for now
    * @param
    */
  def resume(): Unit = {
    if (pausePrivilegeLevel.get() == NoPause) {
      return
    }
    // only privilege level >= current pause privilege level can resume the worker
    pausePrivilegeLevel.set(PauseManager.NoPause)
    unblockDPThread()
  }

  /** check for pause in dp thread
    * only dp thread and operator logic can call this function
    * @throws
    */
  @throws[Exception]
  def checkForPause(): Unit = {
    // returns if not paused
    if (isPauseSet()) {
      blockDPThread()
    }
  }

  def isPauseSet(): Boolean = {
    (pausePrivilegeLevel.get() != PauseManager.NoPause)
  }

  /** block the thread by creating CompletableFuture and wait for completion
    */
  private[this] def blockDPThread(): Unit = {
    // create a future and wait for its completion
    this.dpThreadBlocker = new CompletableFuture[Void]
    // notify main actor thread
    if (promiseFromActorThread != null) {
      controlOutputPort.sendTo(
        VirtualIdentity.Self,
        ReturnPayload(promiseFromActorThread.ctx, ExecutionPaused())
      )
      promiseFromActorThread = null
    }
    // thread blocks here
    this.dpThreadBlocker.get
  }

  /** unblock DP thread by resolving the CompletableFuture
    */
  private[this] def unblockDPThread(): Unit = {
    // If dp thread suspended, release it
    if (dpThreadBlocker != null) {
      this.dpThreadBlocker.complete(null)
      this.dpThreadBlocker = null
    }
  }

}
