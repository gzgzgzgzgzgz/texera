package edu.uci.ics.amber.engine.common.ambermessage.neo

import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

trait InternalMessage extends Serializable {
  val from: Identifier
  val sequenceNumber: Long
}
