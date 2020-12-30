package edu.uci.ics.amber.engine.architecture.common

import akka.actor.{Actor, ActorLogging, Stash}
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.messaginglayer.{
  ControlInputPort,
  ControlOutputPort,
  NetworkOutputGate
}
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity.ActorVirtualIdentity

class WorkflowActor(identifier: ActorVirtualIdentity)
    extends Actor
    with ActorLogging
    with Stash
    with NetworkOutputGate {

  val networkOutput: NetworkOutputGate = this
  lazy val controlInputPort: ControlInputPort = wire[ControlInputPort]
  lazy val controlOutputPort: ControlOutputPort = wire[ControlOutputPort]

  // Not being used right now.
  override def receive: Receive = {
    case other =>
      throw new NotImplementedError("Message other than data message reached WorkflowActor receive")
  }
}
