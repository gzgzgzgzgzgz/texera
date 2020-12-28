package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.architecture.sendsemantics.datatransferpolicy.DataTransferPolicy
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.UpdateInputLinking
import edu.uci.ics.amber.engine.common.ambermessage.neo.DataEvent
import edu.uci.ics.amber.engine.common.ambertag.LinkTag
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier
import edu.uci.ics.amber.engine.common.tuple.ITuple

import scala.util.control.Breaks

/** This class is a container of all the transfer policies.
  * @param amberID
  * @param dataOutputChannel
  * @param controlOutputChannel
  */
class BatchProducer(
    amberID: Identifier,
    dataOutputChannel: DataOutputChannel,
    controlOutputChannel: ControlOutputChannel
) {
  private var policies = new Array[DataTransferPolicy](0)

  /** Add down stream operator and its transfer policy
    * @param policy
    * @param linkTag
    * @param receivers
    */
  def addPolicy(
      policy: DataTransferPolicy,
      linkTag: LinkTag,
      receivers: Array[Identifier]
  ): Unit = {
    var i = 0
    receivers.foreach { x =>
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

  /** Push one tuple to the downstream, will be batched by each transfer policy.
    * Should ONLY be called by DataProcessor.
    * @param tuple
    */
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

  /* Old API: for compatibility */
  def resetPolicies(): Unit = {
    policies.foreach(_.reset())
  }

  /* Send the last batch and EOU marker to all down streams */
  def emitEndMarker(): Unit = {
    var i = 0
    while (i < policies.length) {
      val receiversAndBatches: Array[(Identifier, DataEvent)] = policies(i).noMore()
      receiversAndBatches.foreach {
        case (id, batch) => dataOutputChannel.sendTo(id, batch)
      }
      i += 1
    }
  }

}
