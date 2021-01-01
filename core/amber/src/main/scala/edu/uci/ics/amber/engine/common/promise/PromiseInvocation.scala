package edu.uci.ics.amber.engine.common.promise

/** The invocation of a promise
  * @param context
  * @param call
  */
case class PromiseInvocation(context: PromiseContext, call: PromiseBody[_]) extends PromisePayload
