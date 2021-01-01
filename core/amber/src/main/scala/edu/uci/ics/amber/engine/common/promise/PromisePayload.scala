package edu.uci.ics.amber.engine.common.promise

import edu.uci.ics.amber.engine.common.ambermessage.neo.ControlPayload

/** For now, promise is only one type of control
  * message. I reserved some space for other control
  * message. If no need, ControlPayload can be
  * replaced by this class.
  */
trait PromisePayload extends ControlPayload {
  val context: PromiseContext
}
