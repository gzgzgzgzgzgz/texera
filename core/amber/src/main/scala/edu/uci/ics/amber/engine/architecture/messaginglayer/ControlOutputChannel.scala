package edu.uci.ics.amber.engine.architecture.messaginglayer

import java.util.concurrent.atomic.AtomicLong

import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputChannel.InternalControlMessage
import edu.uci.ics.amber.engine.common.ambermessage.neo.{ControlEvent, InternalMessage}
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

import scala.collection.mutable


class ControlOutputChannel(amberID: Identifier, networkOutput: NetworkOutputGate) {
  private val controlMessageSeqMap = new mutable.AnyRefMap[Identifier, AtomicLong]()

  def sendTo(to: Identifier, event: ControlEvent): Unit = {
    if (to == Identifier.None) {
      return
    }
    val seqNum = controlMessageSeqMap.getOrElseUpdate(to, new AtomicLong()).getAndIncrement()
    val msg = InternalControlMessage(amberID, seqNum, event)
    networkOutput.forwardMessage(to, msg)
  }

}
