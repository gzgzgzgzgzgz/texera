package edu.uci.ics.amber.engine.architecture.messaginglayer

import org.scalatest.flatspec.AnyFlatSpec
import org.scalamock.scalatest.MockFactory
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.sendsemantics.datatransferpolicy.OneToOnePolicy
import edu.uci.ics.amber.engine.common.amberexception.AmberException
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.{DataPayload, EndOfUpstream, UpdateInputLinking}
import edu.uci.ics.amber.engine.common.ambertag.{LayerTag, LinkTag}
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier.ActorIdentifier
import edu.uci.ics.amber.engine.common.tuple.ITuple

class BatchProducerSpec extends AnyFlatSpec with MockFactory {
  private val mockDataOutputPort = mock[DataOutputPort]
  private val mockControlOutputPort = mock[ControlOutputPort]
  private val identifier = ActorIdentifier("batch producer mock")

  "batch producer" should "aggregate tuples and output" in {
    val batchProducer = wire[BatchProducer]
    val tuples = Array.fill(21)(ITuple(1,2,3,4,"5",9.8))
    inSequence{
      (mockControlOutputPort.sendTo _).expects(Identifier.None, UpdateInputLinking(identifier, 0))
      (mockDataOutputPort.sendTo _).expects(Identifier.None, DataPayload(tuples.slice(0, 10)))
      (mockDataOutputPort.sendTo _).expects(Identifier.None, DataPayload(tuples.slice(10, 20)))
      (mockDataOutputPort.sendTo _).expects(Identifier.None, DataPayload(tuples.slice(20, 21)))
      (mockDataOutputPort.sendTo _).expects(Identifier.None, EndOfUpstream())
    }
    val fakeLink = LinkTag(LayerTag("","",""),LayerTag("","",""),0)
    val fakeReceiver = Array[Identifier](Identifier.None)
    batchProducer.addPolicy(new OneToOnePolicy(10),fakeLink,fakeReceiver)
    tuples.foreach{
      t =>
      batchProducer.passTupleToDownstream(t)
    }
    batchProducer.emitEndMarker()
  }


  "batch producer" should "not output tuples when there is no policy" in {
    val batchProducer = wire[BatchProducer]
    val tuples = Array.fill(21)(ITuple(1,2,3,4,"5",9.8))
    (mockDataOutputPort.sendTo _).expects(*,*).never()
    tuples.foreach{
      t =>
        batchProducer.passTupleToDownstream(t)
    }
    batchProducer.emitEndMarker()
  }

}
