package edu.uci.ics.amber.engine.common.promise

import com.typesafe.scalalogging.LazyLogging

import scala.language.experimental.macros

/** The handling logic of a particular promise
  * Every promise control logic should derive
  * from this class.
  */
trait PromiseHandler extends LazyLogging {
  this: PromiseManager =>

  def registerHandler(eventHandler: PartialFunction[PromiseBody[_], Unit]): Unit =
    promiseHandler = eventHandler orElse promiseHandler

}
