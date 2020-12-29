package edu.uci.ics.amber.engine.architecture.common

import akka.actor.{Actor, ActorLogging, Stash}
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.messaginglayer.{
  ControlInputPort,
  ControlOutputPort,
  NetworkOutputGate
}
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

class WorkflowActor(identifier: Identifier)
    extends Actor
    with ActorLogging
    with Stash
    with NetworkOutputGate {

  val networkOutput: NetworkOutputGate = this
  lazy val controlInputChannel: ControlInputPort = wire[ControlInputPort]
  lazy val controlOutputChannel: ControlOutputPort = wire[ControlOutputPort]

  // Not being used right now.
  override def receive: Receive = {
    case other =>
      throw new NotImplementedError("Message other than data message reached WorkflowActor receive")
  }
}
