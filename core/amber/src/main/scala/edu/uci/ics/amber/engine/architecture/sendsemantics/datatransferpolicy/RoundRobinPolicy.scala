package edu.uci.ics.amber.engine.architecture.sendsemantics.datatransferpolicy

import edu.uci.ics.amber.engine.common.ambertag.LinkTag
import edu.uci.ics.amber.engine.common.tuple.ITuple
import akka.actor.{ActorContext, ActorRef}
import akka.event.LoggingAdapter
import akka.util.Timeout
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.{DataPayload, EndOfUpstream}
import edu.uci.ics.amber.engine.common.ambermessage.neo.DataEvent
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext

class RoundRobinPolicy(batchSize: Int) extends DataTransferPolicy(batchSize) {
  var receivers: Array[Identifier] = _
  var roundRobinIndex = 0
  var batch: Array[ITuple] = _
  var currentSize = 0

  override def noMore(): Array[(Identifier, DataEvent)] = {
    val ret = new ArrayBuffer[(Identifier, DataEvent)]
    if (currentSize > 0) {
      ret.append((receivers(roundRobinIndex), DataPayload(batch.slice(0, currentSize)))
      )
    }
    ret.append((receivers(roundRobinIndex), EndOfUpstream()))
    ret.toArray
  }

  override def addTupleToBatch(
      tuple: ITuple
  ): Option[(Identifier, DataEvent)] = {
    batch(currentSize) = tuple
    currentSize += 1
    if (currentSize == batchSize) {
      currentSize = 0
      val retBatch = batch
      roundRobinIndex = (roundRobinIndex + 1) % receivers.length
      batch = new Array[ITuple](batchSize)
      return Some((receivers(roundRobinIndex), DataPayload(retBatch)))
    }
    None
  }

  override def initialize(tag: LinkTag, _receivers: Array[Identifier]): Unit = {
    super.initialize(tag, _receivers)
    assert(_receivers != null)
    this.receivers = _receivers
    batch = new Array[ITuple](batchSize)
  }

  override def reset(): Unit = {
    batch = new Array[ITuple](batchSize)
    roundRobinIndex = 0
    currentSize = 0
  }
}
