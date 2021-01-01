package edu.uci.ics.amber.engine.common.promise


case class ReturnPayload(context: PromiseContext, returnValue: Any) extends PromisePayload