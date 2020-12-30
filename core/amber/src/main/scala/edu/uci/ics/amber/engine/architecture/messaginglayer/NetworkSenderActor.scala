package edu.uci.ics.amber.engine.architecture.messaginglayer

import akka.actor.{Actor, ActorRef, Props, Stash}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkSenderActor.{NetworkMessage, QueryActorRef, RegisterActorRef, SendRequest}
import edu.uci.ics.amber.engine.common.ambermessage.neo.WorkflowMessage
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity.ActorVirtualIdentity

import scala.collection.mutable

object NetworkSenderActor {

  /** to distinguish between main actor self ref and
    * network sender actor
    * TODO: remove this after using Akka Typed APIs
    * @param ref
    */
  case class NetworkSenderActorRef(ref:ActorRef){
    def !(message: Any)(implicit sender: ActorRef = Actor.noSender): Unit = {
      ref ! message
    }
  }

  final case class SendRequest(id:ActorVirtualIdentity, message:WorkflowMessage)

  /** Identifier <-> ActorRef related messages
    */
  final case class QueryActorRef(id: ActorVirtualIdentity, replyTo: Set[ActorRef])
  final case class RegisterActorRef(id: ActorVirtualIdentity, ref: ActorRef)

  /** All outgoing message should be eventually NetworkMessage
    * @param messageID
    * @param internalMessage
    */
  final case class NetworkMessage(messageID: Long, internalMessage: WorkflowMessage)

  /** Ack for NetworkMessage
    * note that it should NEVER be handled by the main thread
    * @param messageID
    */
  final case class NetworkAck(messageID: Long)

  def props(): Props =
    Props(new NetworkSenderActor())
}

/** This actor handles the transformation from identifier to actorRef
  * and also sends message to other actors. This is the most outer part of
  * the messaging layer.
  */
class NetworkSenderActor extends Actor with Stash{

  val idToActorRefs = mutable.HashMap[ActorVirtualIdentity, ActorRef]()
  val messageStash = mutable.HashMap[ActorVirtualIdentity, mutable.Queue[WorkflowMessage]]()

  /** keeps track of every outgoing message.
    * Each message is identified by this monotonic increasing ID.
    * It's different from the sequence number and it will only
    * be used by the output gate.
    */
  var networkMessageID = 0L

  //add self into idMap
  idToActorRefs(VirtualIdentity.Self) = context.parent

  /** This method should always be a part of the unified WorkflowActor receiving logic.
    * 1. when an actor wants to know the actorRef of an Identifier, it replies if the mapping
    *    is known, else it will ask its parent actor.
    * 2. when it receives a mapping, it adds that mapping to the state.
    */
  def findActorRefFromVirtualIdentity: Receive = {
    case QueryActorRef(id, replyTo) =>
      if (idToActorRefs.contains(id)) {
        replyTo.foreach{
          actor => actor ! RegisterActorRef(id, idToActorRefs(id))
        }
      } else {
        context.parent ! QueryActorRef(id, replyTo + self)
      }
    case RegisterActorRef(id, ref) =>
      registerActorRef(id, ref)
  }

  /** This method forward a message by using tell pattern
    * if the map from Identifier to ActorRef is known,
    * forward the message immediately,
    * otherwise stash the message and ask parent for help.
    */
  def forwardMessage(to: ActorVirtualIdentity, msg: WorkflowMessage): Unit = {
    if (idToActorRefs.contains(to)) {
      forward(idToActorRefs(to), msg)
    } else {
      messageStash.getOrElseUpdate(to, new mutable.Queue[WorkflowMessage]()).enqueue(msg)
      context.parent ! QueryActorRef(to, Set(self))
    }
  }

  /** Send message to another actor.
    * @param to
    * @param message
    */
  private def forward(to: ActorRef, message: WorkflowMessage): Unit = {
    to ! NetworkMessage(networkMessageID, message)
    networkMessageID += 1
  }

  /** Add one mapping from Identifier to ActorRef into its state.
    * If there are unsent messages for the actor, send them.
    * @param id
    * @param ref
    */
  def registerActorRef(id: ActorVirtualIdentity, ref: ActorRef): Unit = {
    idToActorRefs(id) = ref
    if (messageStash.contains(id)) {
      val stash = messageStash(id)
      while (stash.nonEmpty) {
        forward(ref, stash.dequeue())
      }
    }
  }

  def sendMessages:Receive = {
    case SendRequest(id, msg) =>
      forwardMessage(id, msg)
  }


  override def receive: Receive = {
    sendMessages orElse findActorRefFromVirtualIdentity
  }
}
