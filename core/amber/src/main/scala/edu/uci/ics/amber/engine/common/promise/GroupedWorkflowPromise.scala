package edu.uci.ics.amber.engine.common.promise

import com.twitter.util.Promise

import scala.collection.mutable

// grouped promise, used when a single actor needs to
// send control to a group of actors and wait all the results.
case class GroupedWorkflowPromise[T](startID: Long, endID: Long, promise: WorkflowPromise[Seq[T]]) {

  val returnValues: Array[T] = Array.ofDim[T]((endID - startID).asInstanceOf[Int])
  private val expectedIds: mutable.HashSet[Long] = mutable.HashSet[Long](startID until endID: _*)

  def takeReturnValue(returnEvent: ReturnPayload): Boolean = {
    val retID = returnEvent.context.id
    if (expectedIds.contains(retID)) {
      expectedIds.remove(retID)
      returnValues(retID - startID) = returnEvent.returnValue.asInstanceOf[T]
    }
    expectedIds.isEmpty
  }

  def invoke(): Unit = {
    promise.setValue(returnValues.toSeq)
  }

}
