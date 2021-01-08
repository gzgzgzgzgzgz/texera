package edu.uci.ics.amber.engine.common.promise

import com.twitter.util.Promise
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity.ActorVirtualIdentity

import scala.language.experimental.macros
import scala.reflect.ClassTag

class PromiseHandlerInitializer(promiseManager: PromiseManager) extends LazyLogging {

  // empty promise handler.
  // default behavior: discard
  private var promiseHandler: PartialFunction[PromiseBody[_], Unit] = {
    case promise =>
      logger.info(s"discarding $promise")
  }

  def registerHandler(newPromiseHandler: PartialFunction[PromiseBody[_], Unit]): Unit = {
    promiseHandler = newPromiseHandler orElse promiseHandler
  }

  def schedule[T](cmd: PromiseBody[T], on: ActorVirtualIdentity): Promise[T] = {
    promiseManager.schedule(cmd, on)
  }

  def schedule[T: ClassTag](seq: (PromiseBody[T], ActorVirtualIdentity)*): Promise[Seq[T]] = {
    promiseManager.schedule(seq: _*)
  }

  def returning(value: Any): Unit = {
    promiseManager.returning(value)
  }

  def returning(): Unit = {
    promiseManager.returning()
  }

  def createLocalPromise[T](): WorkflowPromise[T] = {
    promiseManager.createPromise()
  }

  def getPromiseHandlers: PartialFunction[PromiseBody[_], Unit] = promiseHandler

}
