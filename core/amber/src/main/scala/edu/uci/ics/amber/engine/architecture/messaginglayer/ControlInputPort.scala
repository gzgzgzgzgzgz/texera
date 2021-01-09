package edu.uci.ics.amber.engine.architecture.messaginglayer

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort.WorkflowControlMessage
import edu.uci.ics.amber.engine.common.WorkflowLogger
import edu.uci.ics.amber.engine.common.ambermessage.neo.{ControlPayload, WorkflowMessage}
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity
import edu.uci.ics.amber.engine.common.promise.PromiseManager

import scala.collection.mutable

object ControlInputPort {
  final case class WorkflowControlMessage(
      from: VirtualIdentity,
      sequenceNumber: Long,
      payload: ControlPayload
  ) extends WorkflowMessage
}

class ControlInputPort(promiseManager: PromiseManager) {

  protected val logger: WorkflowLogger = WorkflowLogger("ControlInputPort")

  private val idToOrderingEnforcers =
    new mutable.AnyRefMap[VirtualIdentity, OrderingEnforcer[ControlPayload]]()

  def handleControlMessage(msg: WorkflowControlMessage): Unit = {
    OrderingEnforcer.reorderMessage(
      idToOrderingEnforcers,
      msg.from,
      msg.sequenceNumber,
      msg.payload
    ) match {
      case Some(iterable) =>
        processControlPayload(iterable)
      case None =>
        // discard duplicate
        println(s"receive duplicated: ${msg.payload}")
    }
  }

  @inline
  private def processControlPayload(iter: Iterable[ControlPayload]): Unit = {
    iter.foreach {
      case p: ControlPayload =>
        promiseManager.execute(p)
      case other =>
        logger.logInfo(s"received control message which we cannot handle: $other")
      //skip for now
    }
  }
}
