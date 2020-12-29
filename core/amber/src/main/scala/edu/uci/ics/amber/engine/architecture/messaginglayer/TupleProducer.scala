package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.architecture.worker.neo.WorkerInternalQueue
import edu.uci.ics.amber.engine.architecture.worker.neo.WorkerInternalQueue.{EndMarker, EndOfAllMarker, InputTuple, SenderChangeMarker}
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.{DataPayload, EndOfUpstream}
import edu.uci.ics.amber.engine.common.ambermessage.neo.DataEvent
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

import scala.collection.mutable

class TupleProducer(workerInternalQueue: WorkerInternalQueue) {

  /**
    * Map from Identifier to input number. Used to convert the Identifier
    * to int when adding sender info to the queue.
    * We also keep track of the upstream actors so that we can emit
    * EndOfAllMarker when all upstream actors complete their job
    */
  private val inputMap = new mutable.HashMap[Identifier, Int]
  private val upstreamMap = new mutable.HashMap[Int, mutable.HashSet[Identifier]]
  private var currentSender = -1

  def registerInput(identifier: Identifier, input: Int): Unit = {
    upstreamMap.getOrElseUpdate(input, new mutable.HashSet[Identifier]()).add(identifier)
    inputMap(identifier) = input
  }

  /** This method handles various data events and put different
    * element into the internal queue.
    * data events:
    * 1. Data Payload, it will be split into tuples and add to the queue.
    * 2. End Of Upstream, this event will be received once per upstream actor.
    *    Note that multiple upstream actors can be there for one upstream.
    *    We emit EOU marker when one upstream exhausts. Also, we emit End Of All marker
    *    when ALL upstreams exhausts.
    *
    * @param from
    * @param dataEvents
    */
  def processDataEvents(from:Identifier, dataEvents: Iterable[DataEvent]): Unit ={
    val sender = inputMap(from)
    if (currentSender != sender) {
      workerInternalQueue.appendElement(SenderChangeMarker(sender))
      currentSender = sender
    }
    dataEvents.foreach{
      case DataPayload(payload) =>
        payload.foreach { i =>
          workerInternalQueue.appendElement(InputTuple(i))
        }
      case EndOfUpstream() =>
        upstreamMap(sender).remove(from)
        if (upstreamMap(sender).isEmpty) {
          workerInternalQueue.appendElement(EndMarker())
          upstreamMap.remove(sender)
        }
        if (upstreamMap.isEmpty) {
          workerInternalQueue.appendElement(EndOfAllMarker())
        }
      case other =>
        throw new NotImplementedError()
    }
  }


}
