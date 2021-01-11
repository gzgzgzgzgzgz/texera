package edu.uci.ics.amber.engine.common.promise

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlOutputPort
import edu.uci.ics.amber.engine.common.promise.RPCClient.{ControlInvocation, ReturnPayload}
import edu.uci.ics.amber.engine.common.promise.RPCServer.{AsyncRPCCommand, NormalRPCCommand}

object RPCServer {

  sealed trait RPCCommand[T]
  trait NormalRPCCommand[T] extends RPCCommand[T]
  trait AsyncRPCCommand[T] extends RPCCommand[T]
}

class RPCServer(controlOutputPort: ControlOutputPort) {

  // all handlers
  protected var normalHandlers: PartialFunction[NormalRPCCommand[_], Any] = _

  protected var asyncHandlers: PartialFunction[AsyncRPCCommand[_], Future[_]] = _

  def registerHandler[T](newHandler: PartialFunction[NormalRPCCommand[T], T]): Unit = {
    if (normalHandlers != null) {
      normalHandlers =
        newHandler.asInstanceOf[PartialFunction[NormalRPCCommand[_], Any]] orElse normalHandlers
    } else {
      normalHandlers = newHandler.asInstanceOf[PartialFunction[NormalRPCCommand[_], Any]]
    }
  }

  def registerHandlerAsync[T](newHandler: PartialFunction[AsyncRPCCommand[T], Future[T]]): Unit = {
    if (asyncHandlers != null) {
      asyncHandlers =
        newHandler.asInstanceOf[PartialFunction[AsyncRPCCommand[_], Future[_]]] orElse asyncHandlers
    } else {
      asyncHandlers = newHandler.asInstanceOf[PartialFunction[AsyncRPCCommand[_], Future[_]]]
    }
  }

  def execute(control: ControlInvocation): Unit = {
    try {
      control.call match {
        case command: AsyncRPCCommand[_] =>
          val f = asyncHandlers(command)
          f.onSuccess { ret =>
            returning(control.context, ret)
          }
          f.onFailure { err =>
            returning(control.context, err)
          }
        case command: NormalRPCCommand[_] =>
          val ret = normalHandlers(command)
          returning(control.context, ret)
      }
    } catch {
      case e: Throwable =>
        // if error occurs, return it to the sender.
        returning(control.context, e)
    }
  }

  private def returning(ctx: RPCContext, ret: Any): Unit = {
    if (ctx != null) {
      controlOutputPort.sendTo(ctx.sender, ReturnPayload(ctx.id, ret))
    }
  }

}
