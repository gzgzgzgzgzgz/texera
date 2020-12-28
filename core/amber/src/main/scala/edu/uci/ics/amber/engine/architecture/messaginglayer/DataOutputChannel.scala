package edu.uci.ics.amber.engine.architecture.messaginglayer

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.ConcurrentHashMap

import edu.uci.ics.amber.engine.architecture.messaginglayer.DataInputChannel.InternalDataMessage
import edu.uci.ics.amber.engine.common.ambermessage.neo.{DataEvent, InternalMessage}
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

import scala.collection.mutable

class DataOutputChannel(amberID:Identifier, networkOutput: NetworkOutputGate) {

  private val dataMessageSeqMap = new mutable.AnyRefMap[Identifier, AtomicLong]()

  def sendTo(to: Identifier, event: DataEvent): Unit = {
    if (to == Identifier.None) {
      return
    }
    val msg = InternalDataMessage(
      amberID,
      dataMessageSeqMap.getOrElseUpdate(to, new AtomicLong()).getAndIncrement(),
      event,
    )
    networkOutput.forwardMessage(to, msg)
  }


}
