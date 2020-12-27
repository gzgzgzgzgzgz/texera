package edu.uci.ics.amber.engine.architecture.messaginglayer

import akka.actor.{Actor, ActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutput.{QueryActorRef, ReplyActorRef}
import edu.uci.ics.amber.engine.common.ambermessage.neo.InternalMessage
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

import scala.collection.mutable

object NetworkOutput{
  final case class QueryActorRef(id: Identifier)

  final case class ReplyActorRef(id: Identifier, ref: ActorRef)
}


trait NetworkOutput {
  this: Actor =>

  private val idMap = mutable.HashMap[Identifier, ActorRef]()
  private val messageStash = mutable.HashMap[Identifier, mutable.Queue[InternalMessage]]()

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
    to ! message
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
