package edu.uci.ics.amber.engine.common.promise

import com.twitter.util.Promise
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlOutputPort
import edu.uci.ics.amber.engine.common.ambermessage.neo.ControlPayload
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.promise.RPCClient.{ControlInvocation, ReturnPayload}
import edu.uci.ics.amber.engine.common.promise.RPCServer.RPCCommand

import scala.collection.mutable

object RPCClient {

  /** The invocation of a control command
    * @param context
    * @param call
    */
  case class ControlInvocation(context: RPCContext, call: RPCCommand[_]) extends ControlPayload

  /** The return message of a promise.
    * @param returnValue
    */
  case class ReturnPayload(id: Long, returnValue: Any) extends ControlPayload

}

class RPCClient(senderID: ActorVirtualIdentity, controlOutputPort: ControlOutputPort) {

  private var promiseID = 0L

  private val unfulfilledPromises = mutable.LongMap[WorkflowPromise[_]]()

  def send[T](cmd: RPCCommand[T], to: ActorVirtualIdentity): Promise[T] = {
    val (p, id) = createPromise[T]()
    controlOutputPort.sendTo(to, ControlInvocation(RPCContext(senderID, id), cmd))
    p
  }

  def createPromise[T](): (Promise[T], Long) = {
    promiseID += 1
    val promise = new WorkflowPromise[T]()
    unfulfilledPromises(promiseID) = promise
    (promise, promiseID)
  }

  def fulfill(ret: ReturnPayload): Unit = {
    if (unfulfilledPromises.contains(ret.id)) {
      val p = unfulfilledPromises(ret.id)
      p.setValue(ret.returnValue.asInstanceOf[p.returnType])
    }
  }

}
