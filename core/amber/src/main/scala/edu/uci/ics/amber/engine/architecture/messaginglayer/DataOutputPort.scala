package edu.uci.ics.amber.engine.architecture.messaginglayer

import java.util.concurrent.atomic.AtomicLong

import akka.actor.ActorRef
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.messaginglayer.DataInputPort.WorkflowDataMessage
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkSenderActor.{
  NetworkSenderActorRef,
  SendRequest
}
import edu.uci.ics.amber.engine.common.ambermessage.neo.DataPayload
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity.ActorVirtualIdentity

import scala.collection.mutable

/** This class handles the assignment of sequence numbers to data
  * The internal logic can send data messages to other actor without knowing
  * where the actor is and without determining the sequence number.
  */
class DataOutputPort(selfID: ActorVirtualIdentity, networkSenderActor: NetworkSenderActorRef)
    extends LazyLogging {

  private val idToSequenceNums = new mutable.AnyRefMap[ActorVirtualIdentity, AtomicLong]()
  private var c = 0

  def sendTo(to: ActorVirtualIdentity, event: DataPayload): Unit = {
    val msg = WorkflowDataMessage(
      selfID,
      idToSequenceNums.getOrElseUpdate(to, new AtomicLong()).getAndIncrement(),
      event
    )
    c += 1
    if (c % 20 == 0) {
      logger.info(s"send $msg to $to")
    }
    networkSenderActor ! SendRequest(to, msg)
  }

}
