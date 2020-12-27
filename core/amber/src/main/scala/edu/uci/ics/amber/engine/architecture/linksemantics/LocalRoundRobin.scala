package edu.uci.ics.amber.engine.architecture.linksemantics

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.ActorLayer
import edu.uci.ics.amber.engine.architecture.sendsemantics.datatransferpolicy.RoundRobinPolicy
import edu.uci.ics.amber.engine.common.AdvancedMessageSending
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.UpdateOutputLinking
import edu.uci.ics.amber.engine.common.ambertag.LinkTag
import akka.event.LoggingAdapter
import akka.util.Timeout
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier

import scala.concurrent.ExecutionContext

class LocalRoundRobin(from: ActorLayer, to: ActorLayer, batchSize: Int, inputNum: Int)
    extends LinkStrategy(from, to, batchSize, inputNum) {
  override def link()(implicit
      timeout: Timeout,
      ec: ExecutionContext,
      log: LoggingAdapter
  ): Unit = {
    assert(from.isBuilt && to.isBuilt)
    val froms = from.layer.groupBy(actor => actor.path.address.hostPort)
    val tos = to.layer.groupBy(actor => actor.path.address.hostPort)
    val isolatedfroms = froms.keySet.diff(tos.keySet)
    val isolatedtos = tos.keySet.diff(froms.keySet)
    val matched = froms.keySet.intersect(tos.keySet)
    val actorToIdentifier = (from.layer.indices.map(x =>
      from.layer(x) -> from.identifiers(x)
    ) ++ to.layer.indices.map(x =>
      to.layer(x) -> to.identifiers(x)
    )).toMap
    matched.foreach(x => {
      val receivers: Array[Identifier] = (tos(x) ++ isolatedtos.flatMap(x => tos(x))).map(actorToIdentifier)
      froms(x).foreach(y =>
        AdvancedMessageSending.blockingAskWithRetry(
          y,
          UpdateOutputLinking(new RoundRobinPolicy(batchSize), tag, receivers),
          10
        )
      )
    })
    isolatedfroms.foreach(x => {
      froms(x).foreach(y =>
        AdvancedMessageSending.blockingAskWithRetry(
          y,
          UpdateOutputLinking(
            new RoundRobinPolicy(batchSize),
            tag,
            to.identifiers),
          10
        )
      )
    })
  }
}
