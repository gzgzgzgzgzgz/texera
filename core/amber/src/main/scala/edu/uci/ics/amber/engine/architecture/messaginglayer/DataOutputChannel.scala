package edu.uci.ics.amber.engine.architecture.messaginglayer

import java.util.concurrent.atomic.AtomicLong

import edu.uci.ics.amber.engine.architecture.messaginglayer.DataInputChannel.InternalDataMessage
import edu.uci.ics.amber.engine.architecture.messaginglayer.DataOutputChannel.DataMessageAck
import edu.uci.ics.amber.engine.common.ambermessage.neo.{DataEvent, InternalMessage}
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

import scala.collection.mutable

object DataOutputChannel {
  final case class DataMessageAck(messageIdentifier: Long)
}

class DataOutputChannel(amberID:Identifier, networkOutput: NetworkOutput) {


  private val dataMessageSeqMap = new mutable.AnyRefMap[Identifier, AtomicLong]()
  private var dataUUID = 0L
  private val dataMessageInTransit = mutable.LongMap[InternalMessage]()

  def sendTo(to: Identifier, event: DataEvent): Unit = {
    if (to == Identifier.None) {
      return
    }
    val msg = InternalDataMessage(
      amberID,
      dataMessageSeqMap.getOrElseUpdate(to, new AtomicLong()).getAndIncrement(),
      dataUUID,
      event,
    )
    dataUUID += 1
    dataMessageInTransit(dataUUID) = msg
    networkOutput.forwardMessage(to, msg)
  }

  def ackDataMessage(msg:DataMessageAck):Unit = {
    if (dataMessageInTransit.contains(msg.messageIdentifier)) {
      dataMessageInTransit.remove(msg.messageIdentifier)
    }
  }
}
