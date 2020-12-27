package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.architecture.breakpoint.localbreakpoint.LocalBreakpoint
import edu.uci.ics.amber.engine.architecture.sendsemantics.datatransferpolicy.DataTransferPolicy
import edu.uci.ics.amber.engine.architecture.worker.{WorkerState, WorkerStatistics}
import edu.uci.ics.amber.engine.common.ambertag.{LayerTag, LinkTag, WorkerTag}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import akka.actor.{ActorPath, ActorRef}
import edu.uci.ics.amber.engine.common.ambermessage.neo.{ControlEvent, DataEvent, InternalMessage}
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier


object WorkerMessage {

  final case class AckedWorkerInitialization(recoveryInformation: Seq[(Long, Long)] = Nil)
      extends ControlEvent

  final case class UpdateInputLinking(fromLayer: Identifier, inputNum: Int)
      extends ControlEvent

  final case class UpdateOutputLinking(
      policy: DataTransferPolicy,
      link: LinkTag,
      receivers: Array[Identifier]
  ) extends ControlEvent

  final case class EndSending(sequenceNumber: Long) extends ControlEvent

  final case class ExecutionCompleted() extends ControlEvent

  final case class ExecutionPaused() extends ControlEvent

  final case class AssignBreakpoint(breakpoint: LocalBreakpoint) extends ControlEvent

  final case class QueryTriggeredBreakpoints() extends ControlEvent

  final case class QueryBreakpoint(id: String) extends ControlEvent

  final case class ReportState(workerState: WorkerState.Value) extends ControlEvent

  final case class ReportStatistics(workerStatistics: WorkerStatistics) extends ControlEvent

  final case class ReportOutputResult(results: List[ITuple]) extends ControlEvent

  final case class RemoveBreakpoint(id: String) extends ControlEvent

  final case class ReportedTriggeredBreakpoints(bps: Array[LocalBreakpoint]) extends ControlEvent

  final case class ReportedQueriedBreakpoint(bp: LocalBreakpoint) extends ControlEvent

  final case class ReportFailure(exception: Exception) extends ControlEvent

  final case class ReportUpstreamExhausted(tag: LayerTag) extends ControlEvent

  final case class ReportWorkerPartialCompleted(worker: WorkerTag, layer: LayerTag)
      extends ControlEvent

  final case class CheckRecovery() extends ControlEvent

  final case class ReportCurrentProcessingTuple(workerID: ActorPath, tuple: ITuple)
      extends ControlEvent

  final case class Reset(core: Any, recoveryInformation: Seq[(Long, Long)]) extends ControlEvent


  final case class EndOfUpstream() extends DataEvent

  final case class DataPayload(payload: Array[ITuple])
      extends DataEvent {
    override def equals(obj: Any): Boolean = {
      if (!obj.isInstanceOf[DataPayload]) return false
      val other = obj.asInstanceOf[DataPayload]
      if (other eq null) return false
      if (payload.length != other.payload.length) {
        return false
      }
      var i = 0
      while (i < payload.length) {
        if (payload(i) != other.payload(i)) {
          return false
        }
        i += 1
      }
      true
    }
  }
}
