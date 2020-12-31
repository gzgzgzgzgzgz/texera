package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.architecture.messaginglayer.CongestionControl.{maxWindowSize, minWindowSize, timeGap}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkSenderActor.NetworkMessage

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object CongestionControl{
  final val maxWindowSize = 64
  final val minWindowSize = 2
  final val expendThreshold = 4
  final val sendingTimeout: FiniteDuration = 30.seconds
  final val timeGap = 3000 // 3s
}


class CongestionControl {

  var ssThreshold = 16
  var windowSize = minWindowSize
  var sentTimeStamp = 0L
  val toBeSent = new mutable.Queue[NetworkMessage]
  val messageBuffer = new ArrayBuffer[NetworkMessage]()
  private val inTransit = new mutable.LongMap[NetworkMessage]

  def canBeSent(data:NetworkMessage):Boolean = {
    if (inTransit.size < windowSize) {
      sentTimeStamp = System.currentTimeMillis()
      inTransit(data.messageID) = data
      true
    }else{
      println(s"queued $data")
      toBeSent.enqueue(data)
      false
    }
  }

  def ack(id:Long):Array[NetworkMessage] = {
    messageBuffer.clear()
    inTransit.remove(id)
    if (System.currentTimeMillis() - sentTimeStamp < timeGap) {
      if (windowSize < ssThreshold) {
        windowSize = Math.min(windowSize * 2, ssThreshold)
      } else {
        windowSize += 1
      }
    } else {
      ssThreshold /= 2
      windowSize = Math.max(minWindowSize, Math.min(ssThreshold, maxWindowSize))
    }
    while(inTransit.size < windowSize && toBeSent.nonEmpty) {
      val msg = toBeSent.dequeue()
      inTransit(msg.messageID) = msg
      messageBuffer.append(msg)
    }
    messageBuffer.toArray
  }

}
