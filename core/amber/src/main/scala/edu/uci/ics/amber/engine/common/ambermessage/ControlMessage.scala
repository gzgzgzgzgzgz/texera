package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.operators.OpExecConfig
import edu.uci.ics.amber.engine.architecture.breakpoint.FaultedTuple
import edu.uci.ics.amber.engine.architecture.breakpoint.localbreakpoint.LocalBreakpoint
import edu.uci.ics.amber.engine.common.tuple.ITuple
import akka.actor.ActorRef
import edu.uci.ics.amber.engine.common.ambermessage.neo.ControlEvent

object ControlMessage {

  final case class Start() extends ControlEvent

  final case class Pause() extends ControlEvent

  final case class ModifyLogic(newMetadata: OpExecConfig) extends ControlEvent

  final case class Resume() extends ControlEvent

  final case class QueryState() extends ControlEvent

  final case class QueryStatistics() extends ControlEvent

  final case class CollectSinkResults() extends ControlEvent

  final case class LocalBreakpointTriggered() extends ControlEvent

  final case class RequireAck(msg: Any) extends ControlEvent

  final case class Ack() extends ControlEvent

  final case class AckWithInformation(info: Any) extends ControlEvent

  final case class AckWithSequenceNumber(sequenceNumber: Long) extends ControlEvent

  final case class AckOfEndSending() extends ControlEvent

  final case class StashOutput() extends ControlEvent

  final case class ReleaseOutput() extends ControlEvent

  final case class SkipTuple(faultedTuple: FaultedTuple) extends ControlEvent

  final case class SkipTupleGivenWorkerRef(actorPath: String, faultedTuple: FaultedTuple)
      extends ControlEvent

  final case class ModifyTuple(faultedTuple: FaultedTuple) extends ControlEvent

  final case class ResumeTuple(faultedTuple: FaultedTuple) extends ControlEvent

  final case class KillAndRecover() extends ControlEvent
}
