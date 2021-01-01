package edu.uci.ics.amber.engine.common.promise


case class PromiseInvocation(context: PromiseContext, call: PromiseBody[_]) extends PromisePayload

