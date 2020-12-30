package edu.uci.ics.amber.engine.architecture.messaginglayer

import java.util.concurrent.atomic.AtomicLong

import edu.uci.ics.amber.engine.architecture.messaginglayer.DataInputPort.WorkflowDataMessage
import edu.uci.ics.amber.engine.common.ambermessage.neo.DataPayload
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity.ActorVirtualIdentity

import scala.collection.mutable

/** This class handles the assignment of sequence numbers to data
  * The internal logic can send data messages to other actor without knowing
  * where the actor is and without determining the sequence number.
  */
class DataOutputPort(selfID: ActorVirtualIdentity, networkOutput: NetworkOutputGate) {

  private val idToSequenceNums = new mutable.AnyRefMap[ActorVirtualIdentity, AtomicLong]()

  def sendTo(to: ActorVirtualIdentity, event: DataPayload): Unit = {
    val msg = WorkflowDataMessage(
      selfID,
      idToSequenceNums.getOrElseUpdate(to, new AtomicLong()).getAndIncrement(),
      event
    )
    networkOutput.forwardMessage(to, msg)
  }

}
