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

class HashBasedShufflePolicy(batchSize: Int, val hashFunc: ITuple => Int)
    extends DataTransferPolicy(batchSize) {
  var batches: Array[Array[ITuple]] = _
  var receivers: Array[Identifier] = _
  var currentSizes: Array[Int] = _

  override def noMore(): Array[(Identifier, DataEvent)] = {
    val receiversAndBatches = new ArrayBuffer[(Identifier,DataEvent)]
    for (k <- receivers.indices) {
      if (currentSizes(k) > 0) {
        receiversAndBatches.append((receivers(k), DataPayload(batches(k).slice(0, currentSizes(k)))))
      }
      receiversAndBatches.append((receivers(k),EndOfUpstream()))
    }
    receiversAndBatches.toArray
  }

  override def addTupleToBatch(
      tuple: ITuple
  ): Option[(Identifier,DataEvent)] = {
    val numBuckets = receivers.length
    val index = (hashFunc(tuple) % numBuckets + numBuckets) % numBuckets
    batches(index)(currentSizes(index)) = tuple
    currentSizes(index) += 1
    if (currentSizes(index) == batchSize) {
      currentSizes(index) = 0
      val retBatch = batches(index)
      batches(index) = new Array[ITuple](batchSize)
      return Some((receivers(index), DataPayload(retBatch)))
    }
    None
  }

  override def initialize(tag: LinkTag, _receivers: Array[Identifier]): Unit = {
    super.initialize(tag, _receivers)
    assert(_receivers != null)
    this.receivers = _receivers
    initializeInternalState(receivers)
  }

  override def reset(): Unit = {
    initializeInternalState(receivers)
  }

  private[this] def initializeInternalState(_receivers: Array[Identifier]): Unit ={
    batches = new Array[Array[ITuple]](_receivers.length)
    for (i <- _receivers.indices) {
      batches(i) = new Array[ITuple](batchSize)
    }
    currentSizes = new Array[Int](_receivers.length)
  }
}
