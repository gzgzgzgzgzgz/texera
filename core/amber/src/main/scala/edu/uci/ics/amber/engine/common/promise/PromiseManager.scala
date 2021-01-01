package edu.uci.ics.amber.engine.common.promise

import com.twitter.util.{Future, Promise}
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlOutputPort
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity.ActorVirtualIdentity

import scala.collection.mutable

class PromiseManager(selfID:ActorVirtualIdentity, controlOutputPort: ControlOutputPort) extends LazyLogging {

  protected var promiseID = 0L
  protected val unCompletedPromises = new mutable.HashMap[PromiseContext, WorkflowPromise[_]]()
  protected val unCompletedGroupPromises = new mutable.HashSet[GroupedWorkflowPromise[_]]()
  protected var promiseContext: PromiseContext = _
  protected val queuedInvocations = new mutable.Queue[PromisePayload]()
  protected val ongoingSyncPromises = new mutable.HashSet[PromiseContext]()
  protected var syncPromiseRoot: PromiseContext = _

  protected var promiseHandler: PartialFunction[PromiseBody[_], Unit] = { case promise =>
    logger.info(s"discarding $promise")
  }

  def consume(event: PromisePayload): Unit = {
    event match {
      case ret: ReturnPayload =>
        if (unCompletedPromises.contains(ret.context)) {
          val p = unCompletedPromises(ret.context)
          promiseContext = p.ctx
          ret.returnValue match {
            case throwable: Throwable =>
              p.setException(throwable)
            case _ =>
              p.setValue(ret.returnValue.asInstanceOf[p.returnType])
          }
          unCompletedPromises.remove(ret.context)
        }

        for (i <- unCompletedGroupPromises) {
          if (i.takeReturnValue(ret)) {
            promiseContext = i.promise.ctx
            i.invoke()
            unCompletedGroupPromises.remove(i)
          }
        }
      case PromiseInvocation(ctx: RootPromiseContext, call: SynchronizedInvocation) =>
        if (syncPromiseRoot == null) {
          registerSyncPromise(ctx, ctx)
          invokePromise(ctx, call)
        } else {
          queuedInvocations.enqueue(event)
        }
      case PromiseInvocation(ctx: ChildPromiseContext, call: SynchronizedInvocation) =>
        if (syncPromiseRoot == null || ctx.root == syncPromiseRoot) {
          registerSyncPromise(ctx.root, ctx)
          invokePromise(ctx, call)
        } else {
          queuedInvocations.enqueue(event)
        }
      case p: PromiseInvocation =>
        promiseContext = p.context
        try {
          promiseHandler(p.call)
        } catch {
          case e: Throwable =>
            returning(e)
        }
    }
    tryInvokeNextSyncPromise()
  }

  def schedule[T](cmd: PromiseBody[T], on: ActorVirtualIdentity = selfID): Promise[T] = {
    val ctx = mkPromiseContext()
    promiseID += 1
    controlOutputPort.sendTo(on, PromiseInvocation(ctx, cmd))
    val promise = WorkflowPromise[T](promiseContext)
    unCompletedPromises(ctx) = promise
    promise
  }

  def schedule[T](seq: (PromiseBody[T], ActorVirtualIdentity)*): Promise[Seq[T]] = {
    val promise = WorkflowPromise[Seq[T]](promiseContext)
    if (seq.isEmpty) {
      promise.setValue(Seq.empty)
    } else {
      unCompletedGroupPromises.add(GroupedWorkflowPromise[T](promiseID, promiseID + seq.length, promise))
      seq.foreach { i =>
        val ctx = mkPromiseContext()
        promiseID += 1
        controlOutputPort.sendTo(i._2, PromiseInvocation(ctx, i._1))
      }
    }
    promise
  }


  def returning(value: Any): Unit = {
    // returning should be used at most once per context
    if (promiseContext != null) {
      controlOutputPort.sendTo(promiseContext.sender, ReturnPayload(promiseContext, value))
      exitCurrentPromise()
    }
  }

  def returning(): Unit = {
    // returning should be used at most once per context
    if (promiseContext != null) {
      controlOutputPort.sendTo(promiseContext.sender, ReturnPayload(promiseContext, PromiseCompleted()))
      exitCurrentPromise()
    }
  }

  @inline
  private def exitCurrentPromise(): Unit = {
    if (ongoingSyncPromises.contains(promiseContext)) {
      ongoingSyncPromises.remove(promiseContext)
    }
    promiseContext = null
  }

  @inline
  private def tryInvokeNextSyncPromise(): Unit = {
    if (ongoingSyncPromises.isEmpty) {
      syncPromiseRoot = null
      if (queuedInvocations.nonEmpty) {
        consume(queuedInvocations.dequeue())
      }
    }
  }

  @inline
  private def registerSyncPromise(rootCtx: RootPromiseContext, ctx: PromiseContext): Unit = {
    syncPromiseRoot = rootCtx
    ongoingSyncPromises.add(ctx)
  }

  @inline
  protected def mkPromiseContext(): PromiseContext = {
    promiseContext match {
      case null =>
        PromiseContext(selfID, promiseID)
      case ctx: RootPromiseContext =>
        PromiseContext(selfID, promiseID, ctx)
      case ctx: ChildPromiseContext =>
        PromiseContext(selfID, promiseID, ctx.root)
    }
  }

  @inline
  private def invokePromise(ctx: PromiseContext, call: PromiseBody[_]): Unit = {
    promiseContext = ctx
    promiseHandler(call)
  }

}
