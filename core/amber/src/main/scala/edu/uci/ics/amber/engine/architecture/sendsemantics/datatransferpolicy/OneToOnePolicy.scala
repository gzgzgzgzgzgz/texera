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

class OneToOnePolicy(batchSize: Int) extends DataTransferPolicy(batchSize) {
  var receiver: Identifier = _
  var batch: Array[ITuple] = _
  var currentSize = 0

  override def addTupleToBatch(
      tuple: ITuple
  ): Option[(Identifier, DataEvent)] = {
    batch(currentSize) = tuple
    currentSize += 1
    if (currentSize == batchSize) {
      currentSize = 0
      val retBatch = batch
      batch = new Array[ITuple](batchSize)
      return Some((receiver, DataPayload(retBatch)))
    }
    None
  }

  override def noMore(): Array[(Identifier, DataEvent)] = {
    val ret = new ArrayBuffer[(Identifier, DataEvent)]
    if (currentSize > 0) {
      ret.append((receiver, DataPayload(batch.slice(0, currentSize))))
    }
    ret.append((receiver, EndOfUpstream()))
    ret.toArray
  }

  override def initialize(tag: LinkTag, _receivers: Array[Identifier]): Unit = {
    super.initialize(tag, _receivers)
    assert(_receivers != null && _receivers.length == 1)
    receiver = _receivers(0)
    batch = new Array[ITuple](batchSize)
  }

  override def reset(): Unit = {
    batch = new Array[ITuple](batchSize)
    currentSize = 0
  }
}
