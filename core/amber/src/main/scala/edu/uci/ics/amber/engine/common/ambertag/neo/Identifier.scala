package edu.uci.ics.amber.engine.common.ambertag.neo

trait Identifier

object Identifier {

  case class EmptyIdentifier() extends Identifier
  case class ActorIdentifier(name: String) extends Identifier
  case class ClientIdentifier() extends Identifier
  case class ControllerIdentifier() extends Identifier
  case class SelfIdentifier() extends Identifier

  lazy val Controller: ControllerIdentifier = ControllerIdentifier()
  lazy val Client: ClientIdentifier = ClientIdentifier()
  lazy val None: EmptyIdentifier = EmptyIdentifier()
  lazy val Self: SelfIdentifier = SelfIdentifier()
}
