package edu.uci.ics.amber.engine.common.promise

import com.typesafe.scalalogging.LazyLogging

import scala.language.experimental.macros

trait PromiseHandler extends LazyLogging {
  this: PromiseManager =>

  def registerHandler(eventHandler: PartialFunction[PromiseBody[_], Unit]): Unit =
    promiseHandler = eventHandler orElse promiseHandler

}
