package edu.uci.ics.amber.engine.architecture.messaginglayer

import akka.actor.{Actor, ActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputGate.{
  NetworkMessage,
  QueryActorRef,
  ReplyActorRef
}
import edu.uci.ics.amber.engine.common.ambermessage.neo.InternalMessage
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

import scala.collection.mutable

object NetworkOutputGate {

  /** Identifier <-> ActorRef related messages
    */
  final case class QueryActorRef(id: Identifier)
  final case class ReplyActorRef(id: Identifier, ref: ActorRef)

  /** All outgoing message should be eventually NetworkMessage
    * @param uuid
    * @param internalMessage
    */
  final case class NetworkMessage(uuid: Long, internalMessage: InternalMessage)

  /** Ack for NetworkMessage
    * note that it should NEVER be handled by the main thread
    * @param uuid
    */
  final case class NetworkAck(uuid: Long)
}

/** This trait handles the transformation from identifier to actorRef
  * and also sends message to other actors. This is the most outer part of
  * the messaging layer.
  * It SHOULD be a trait since it is highly coupled with actors' sending/receiving logic.
  * TODO: this trait should eventually become another actor, which is the sender actor.
  */
trait NetworkOutputGate {
  this: Actor => // it requires the class to be an actor.

  private val idMap = mutable.HashMap[Identifier, ActorRef]()
  private val messageStash = mutable.HashMap[Identifier, mutable.Queue[InternalMessage]]()
  private var messageUUID = 0L

  //add self into idMap
  idMap(Identifier.Self) = self

  /** This method should always be a part of the unified WorkflowActor receiving logic.
    * 1. when an actor wants to know the actorRef of an Identifier, it replies if the mapping
    *    is known, else it will ask its parent actor.
    * 2. when it receives a mapping, it adds that mapping to the state.
    */
  def findActorRefAutomatically: Receive = {
    case QueryActorRef(id) =>
      if (idMap.contains(id)) {
        sender ! ReplyActorRef(id, idMap(id))
      } else {
        context.parent.tell(QueryActorRef(id), sender)
      }
    case ReplyActorRef(id, ref) =>
      registerActorRef(id, ref)
  }

  /** This method forward a message by using tell pattern
    * if the map from Identifier to ActorRef is known,
    * forward the message immediately,
    * otherwise stash the message and ask parent for help.
    */
  def forwardMessage(to: Identifier, message: InternalMessage): Unit = {
    if (idMap.contains(to)) {
      forward(idMap(to), message)
    } else {
      messageStash.getOrElseUpdate(to, new mutable.Queue[InternalMessage]()).enqueue(message)
      context.parent ! QueryActorRef(to)
    }
  }

  /** Send message to another actor.
    * @param to
    * @param message
    */
  private def forward(to: ActorRef, message: InternalMessage): Unit = {
    to ! NetworkMessage(messageUUID, message)
    messageUUID += 1
  }

  /** Add one mapping from Identifier to ActorRef into its state.
    * If there are unsent messages for the actor, send them.
    * @param id
    * @param ref
    */
  def registerActorRef(id: Identifier, ref: ActorRef): Unit = {
    idMap(id) = ref
    if (messageStash.contains(id)) {
      val stash = messageStash(id)
      while (stash.nonEmpty) {
        forward(ref, stash.dequeue())
      }
    }
  }

}
