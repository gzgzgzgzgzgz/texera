package edu.uci.ics.amber.engine.architecture.messaginglayer

import akka.actor.{Actor, ActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputGate.{NetworkMessage, QueryActorRef, ReplyActorRef}
import edu.uci.ics.amber.engine.common.ambermessage.neo.InternalMessage
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

import scala.collection.mutable

object NetworkOutputGate{
  final case class QueryActorRef(id: Identifier)
  final case class ReplyActorRef(id: Identifier, ref: ActorRef)
  final case class NetworkMessage(uuid:Long, internalMessage: InternalMessage)
  final case class NetworkAck(uuid:Long)
}


trait NetworkOutputGate {
  this: Actor =>

  private val idMap = mutable.HashMap[Identifier, ActorRef]()
  private val messageStash = mutable.HashMap[Identifier, mutable.Queue[InternalMessage]]()
  private var messageUUID = 0L

  //add self into idMap
  idMap(Identifier.Self) = self

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

  def forwardMessage(to: Identifier, message: InternalMessage): Unit = {
    if (idMap.contains(to)) {
      forwardInternal(idMap(to), message)
    } else {
      messageStash.getOrElseUpdate(to, new mutable.Queue[InternalMessage]()).enqueue(message)
      context.parent ! QueryActorRef(to)
    }
  }

  private def forwardInternal(to: ActorRef, message: InternalMessage): Unit = {
    to ! NetworkMessage(messageUUID,message)
    messageUUID += 1
  }

  def registerActorRef(id: Identifier, ref: ActorRef): Unit = {
    idMap(id) = ref
    if (messageStash.contains(id)) {
      val stash = messageStash(id)
      while (stash.nonEmpty) {
        forwardInternal(ref, stash.dequeue())
      }
    }
  }

}
