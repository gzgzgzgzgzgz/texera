package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.architecture.messaginglayer.DataInputChannel.InternalDataMessage
import edu.uci.ics.amber.engine.architecture.worker.neo.WorkerInternalQueue
import edu.uci.ics.amber.engine.architecture.worker.neo.WorkerInternalQueue.{EndMarker, EndOfAllMarker, InputTuple, SenderChangeMarker}
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.{DataPayload, EndOfUpstream}
import edu.uci.ics.amber.engine.common.ambermessage.neo.{DataEvent, InternalMessage}
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

import scala.collection.mutable

object DataInputChannel {
  final case class InternalDataMessage(
                                        from: Identifier,
                                        sequenceNumber: Long,
                                        messageIdentifier: Long,
                                        command: DataEvent,
                                      ) extends InternalMessage
}



class DataInputChannel(workerInternalQueue: WorkerInternalQueue) {
  private val dataOrderingEnforcer =
    new mutable.AnyRefMap[Identifier, OrderingEnforcer[DataEvent]]()
  private var currentSender = -1

  /**
    * map from Identifier to input number. Used to convert the Identifier
    * to int when adding sender info to the queue.
    */
  private val inputMap = new mutable.HashMap[Identifier, Int]
  private val upstreamMap = new mutable.HashMap[Int, mutable.HashSet[Identifier]]

  def registerInput(identifier: Identifier, input:Int): Unit = {
    upstreamMap.getOrElseUpdate(input,new mutable.HashSet[Identifier]()).add(identifier)
    inputMap(identifier) = input
  }

  def handleDataMessage(msg:InternalDataMessage): Unit = {
    OrderingEnforcer.reorderMessage(dataOrderingEnforcer, msg.from, msg.sequenceNumber, msg.command) match {
      case Some(iterable) =>
        val sender = inputMap(msg.from)
        if(currentSender != sender){
          workerInternalQueue.appendElement(SenderChangeMarker(sender))
          currentSender = sender
        }
        iterable.foreach{
          case DataPayload(payload) =>
            payload.foreach{
              i =>
                workerInternalQueue.appendElement(InputTuple(i))
            }
          case EndOfUpstream() =>
            upstreamMap(sender).remove(msg.from)
            if(upstreamMap(sender).isEmpty){
              workerInternalQueue.appendElement(EndMarker())
              upstreamMap.remove(sender)
            }
            if(upstreamMap.isEmpty){
              workerInternalQueue.appendElement(EndOfAllMarker())
            }
          case other =>
            throw new NotImplementedError()
        }
      case None =>
      // discard duplicate
    }
  }

}
