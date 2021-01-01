package edu.uci.ics.amber.engine.common.promise

import com.twitter.util.Promise

object WorkflowPromise {
  def apply[T](ctx: PromiseContext): WorkflowPromise[T] = new WorkflowPromise[T](ctx)
}

class WorkflowPromise[T](val ctx: PromiseContext) extends Promise[T] {

  type returnType = T

  override def setValue(result: returnType): Unit = super.setValue(result)
}
