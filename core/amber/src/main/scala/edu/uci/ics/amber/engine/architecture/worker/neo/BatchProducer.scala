package edu.uci.ics.amber.engine.architecture.worker.neo

import edu.uci.ics.amber.engine.architecture.messaginglayer.{ControlOutputChannel, DataOutputChannel}
import edu.uci.ics.amber.engine.architecture.sendsemantics.datatransferpolicy.DataTransferPolicy
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.UpdateInputLinking
import edu.uci.ics.amber.engine.common.ambermessage.neo.DataEvent
import edu.uci.ics.amber.engine.common.ambertag.LinkTag
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier
import edu.uci.ics.amber.engine.common.tuple.ITuple

import scala.util.control.Breaks

class BatchProducer(amberID:Identifier, dataOutputChannel: DataOutputChannel, controlOutputChannel: ControlOutputChannel) {
  private var policies = new Array[DataTransferPolicy](0)

  def addPolicy(policy: DataTransferPolicy, linkTag:LinkTag, receivers:Array[Identifier]): Unit = {
    var i = 0
    receivers.foreach{
     x =>
       controlOutputChannel.sendTo(x, UpdateInputLinking(amberID, linkTag.inputNum))
    }
    policy.initialize(linkTag, receivers)
    Breaks.breakable {
      while (i < policies.length) {
        if (policies(i).tag == policy.tag) {
          policies(i) = policy
          Breaks.break()
        }
        i += 1
      }
      policies = policies :+ policy
    }
  }


  def passTupleToDownstream(tuple: ITuple): Unit = {
    var i = 0
    while (i < policies.length) {
      val receiverAndBatch: Option[(Identifier, DataEvent)] =
        policies(i).addTupleToBatch(tuple)
      receiverAndBatch match {
        case Some((id, batch)) =>
          // send it to messaging layer to be sent downstream
          dataOutputChannel.sendTo(id, batch)
        case None =>
        // Do nothing
      }
      i += 1
    }
  }

  def resetPolicies(): Unit = {
    policies.foreach(_.reset())
  }

  def emitEndMarker():Unit = {
    var i = 0
    while (i < policies.length) {
      val receiversAndBatches: Array[(Identifier, DataEvent)] = policies(i).noMore()
      receiversAndBatches.foreach{
        case (id, batch) => dataOutputChannel.sendTo(id, batch)
      }
      i += 1
    }
  }

}
