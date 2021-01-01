package edu.uci.ics.amber.engine.common.promise

/** The return message of a promise.
  * It will be created and sent when calling
  * returning() in the handler.
  * @param context
  * @param returnValue
  */
case class ReturnPayload(context: PromiseContext, returnValue: Any) extends PromisePayload
