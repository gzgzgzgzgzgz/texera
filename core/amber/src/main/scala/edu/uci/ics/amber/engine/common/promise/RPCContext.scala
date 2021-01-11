package edu.uci.ics.amber.engine.common.promise

import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity.ActorVirtualIdentity

case class RPCContext(sender: ActorVirtualIdentity, id: Long)
