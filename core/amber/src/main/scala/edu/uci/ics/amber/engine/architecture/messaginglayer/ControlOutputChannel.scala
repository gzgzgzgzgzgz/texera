package edu.uci.ics.amber.engine.architecture.messaginglayer

import java.util.concurrent.atomic.AtomicLong

import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputChannel.InternalControlMessage
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlOutputChannel.ControlMessageAck
import edu.uci.ics.amber.engine.common.ambermessage.neo.{ControlEvent, InternalMessage}
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

import scala.collection.mutable

object ControlOutputChannel {
  final case class ControlMessageAck(messageIdentifier: Long)
}


class ControlOutputChannel(amberID: Identifier, networkOutput: NetworkOutput) {
  private val controlMessageSeqMap = new mutable.AnyRefMap[Identifier, AtomicLong]()
  private var controlUUID = 0L
  private val controlMessageInTransit = mutable.LongMap[InternalMessage]()

  def sendTo(to: Identifier, event: ControlEvent): Unit = {
    if (to == Identifier.None) {
      return
    }
    val seqNum = controlMessageSeqMap.getOrElseUpdate(to, new AtomicLong()).getAndIncrement()
    val msg = InternalControlMessage(amberID, seqNum, controlUUID, event)
    controlMessageInTransit(controlUUID) = msg
    networkOutput.forwardMessage(to, msg)
    controlUUID += 1
  }

  def ackControlMessage(msg:ControlMessageAck):Unit = {
    if (controlMessageInTransit.contains(msg.messageIdentifier)) {
      controlMessageInTransit.remove(msg.messageIdentifier)
    }
  }

}
