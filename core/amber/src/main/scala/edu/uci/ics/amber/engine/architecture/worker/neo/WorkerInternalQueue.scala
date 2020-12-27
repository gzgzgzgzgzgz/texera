package edu.uci.ics.amber.engine.architecture.worker.neo

import java.util.concurrent.LinkedBlockingDeque

import edu.uci.ics.amber.engine.architecture.worker.neo.WorkerInternalQueue.{DummyInput, EndMarker, InputTuple, InternalQueueElement, SenderChangeMarker}
import edu.uci.ics.amber.engine.common.InputExhausted
import edu.uci.ics.amber.engine.common.ambertag.LayerTag
import edu.uci.ics.amber.engine.common.tuple.ITuple

import scala.collection.mutable
import com.typesafe.scalalogging.{LazyLogging, Logger}
import edu.uci.ics.amber.engine.common.amberexception.AmberException

object WorkerInternalQueue {
  // 4 kinds of elements can be accepted by internal queue
  trait InternalQueueElement

  case class InputTuple(tuple: ITuple) extends InternalQueueElement
  case class SenderChangeMarker(newSenderRef: Int) extends InternalQueueElement
  case class EndMarker() extends InternalQueueElement
  case class EndOfAllMarker() extends InternalQueueElement

  /**
    * Used to unblock the dp thread when pause arrives but
    * dp thread is blocked waiting for the next element in the
    * worker-internal-queue
    */
  case class DummyInput() extends InternalQueueElement
}

trait WorkerInternalQueue extends LazyLogging {
  // blocking deque for batches:
  // main thread put batches into this queue
  // tuple input (dp thread) take batches from this queue
  protected val blockingDeque = new LinkedBlockingDeque[InternalQueueElement]

  def isQueueEmpty:Boolean = blockingDeque.isEmpty

  def appendElement(elem:InternalQueueElement): Unit ={
    blockingDeque.add(elem)
  }

  def prependElement(elem:InternalQueueElement):Unit = {
    blockingDeque.addFirst(elem)
  }

}
