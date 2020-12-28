package edu.uci.ics.amber.engine.architecture.messaginglayer

import com.typesafe.scalalogging.{LazyLogging, Logger}
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputChannel.InternalControlMessage
import edu.uci.ics.amber.engine.common.ambermessage.neo.{ControlEvent, InternalMessage}
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

import scala.collection.mutable

object ControlInputChannel {
  final case class InternalControlMessage(
                                           from: Identifier,
                                           sequenceNumber: Long,
                                           command: ControlEvent,
                                         ) extends InternalMessage
}

class ControlInputChannel extends LazyLogging{

  private val controlOrderingEnforcer =
    new mutable.AnyRefMap[Identifier, OrderingEnforcer[ControlEvent]]()

  def handleControlMessage(msg:InternalControlMessage): Unit = {
    OrderingEnforcer.reorderMessage(controlOrderingEnforcer, msg.from, msg.sequenceNumber, msg.command) match {
      case Some(iterable) =>
        processControlEvents(iterable)
      case None =>
        // discard duplicate
        println(s"receive duplicated: ${msg.command}")
    }
  }

  @inline
  private def processControlEvents(iter: Iterable[ControlEvent]): Unit = {
    iter.foreach {
      case other =>
      //skip
    }
  }
}
