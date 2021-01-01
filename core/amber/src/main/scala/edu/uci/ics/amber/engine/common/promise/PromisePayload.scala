package edu.uci.ics.amber.engine.common.promise

import edu.uci.ics.amber.engine.common.ambermessage.neo.ControlPayload

trait PromisePayload extends ControlPayload {
  val context: PromiseContext
}
