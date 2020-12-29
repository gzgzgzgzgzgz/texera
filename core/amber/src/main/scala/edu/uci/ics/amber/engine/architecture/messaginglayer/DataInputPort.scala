package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.architecture.messaginglayer.DataInputPort.InternalDataMessage
import edu.uci.ics.amber.engine.architecture.worker.neo.WorkerInternalQueue
import edu.uci.ics.amber.engine.architecture.worker.neo.WorkerInternalQueue.{
  EndMarker,
  EndOfAllMarker,
  InputTuple,
  SenderChangeMarker
}
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.{DataPayload, EndOfUpstream}
import edu.uci.ics.amber.engine.common.ambermessage.neo.{DataEvent, InternalMessage}
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

import scala.collection.mutable

object DataInputPort {
  final case class InternalDataMessage(
      from: Identifier,
      sequenceNumber: Long,
      command: DataEvent
  ) extends InternalMessage
}

class DataInputPort(tupleProducer: TupleProducer) {
  private val dataOrderingEnforcer =
    new mutable.AnyRefMap[Identifier, OrderingEnforcer[DataEvent]]()

  def handleDataMessage(msg: InternalDataMessage): Unit = {
    OrderingEnforcer.reorderMessage(
      dataOrderingEnforcer,
      msg.from,
      msg.sequenceNumber,
      msg.command
    ) match {
      case Some(iterable) =>
        tupleProducer.processDataEvents(msg.from,iterable)
      case None =>
      // discard duplicate
    }
  }

}
