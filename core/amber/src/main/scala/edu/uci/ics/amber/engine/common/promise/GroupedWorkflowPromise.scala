package edu.uci.ics.amber.engine.common.promise

import com.twitter.util.Promise

import scala.collection.mutable

case class GroupedWorkflowPromise[T](startID: Long, endID: Long, promise: WorkflowPromise[Seq[T]]) {

  val returnValues: mutable.ArrayBuffer[T] = mutable.ArrayBuffer[T]()
  private val expectedIds: mutable.HashSet[Long] = mutable.HashSet[Long](startID until endID: _*)

  def takeReturnValue(returnEvent: ReturnPayload): Boolean = {
    if (expectedIds.contains(returnEvent.context.id)) {
      expectedIds.remove(returnEvent.context.id)
      returnValues.append(returnEvent.returnValue.asInstanceOf[T])
    }
    expectedIds.isEmpty
  }

  def invoke(): Unit = {
    promise.setValue(returnValues.toSeq)
  }

}
