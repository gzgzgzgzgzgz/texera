package edu.uci.ics.amber.engine.architecture.common

import akka.actor.{Actor, ActorLogging, Stash}
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.messaginglayer.{ControlInputChannel, ControlOutputChannel, NetworkOutputGate}
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

class WorkflowActor(identifier: Identifier) extends Actor with ActorLogging with Stash with NetworkOutputGate {

  val networkOutput:NetworkOutputGate = this
  lazy val controlInputChannel: ControlInputChannel = wire[ControlInputChannel]
  lazy val controlOutputChannel: ControlOutputChannel = wire[ControlOutputChannel]

  // Not being used right now.
  override def receive: Receive = {
    case other =>
      throw new NotImplementedError("Message other than data message reached WorkflowActor receive")
  }
}
