package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.architecture.worker.neo.WorkerInternalQueue
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.worker.neo.WorkerInternalQueue.{EndMarker, EndOfAllMarker, InputTuple, SenderChangeMarker}
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.{DataPayload, EndOfUpstream}
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier.ActorIdentifier
import edu.uci.ics.amber.engine.common.tuple.ITuple
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec

class TupleProducerSpec extends AnyFlatSpec with MockFactory{
  private val mockInternalQueue = mock[WorkerInternalQueue]

  "tuple producer" should "break batch into tuples and output" in{
    val tupleProducer = wire[TupleProducer]
    val inputBatch = DataPayload(Array.fill(4)(ITuple(1,2,3,5,"9.8",7.6)))
    inSequence{
      (mockInternalQueue.appendElement _).expects(SenderChangeMarker(0))
      inputBatch.payload.foreach{
        i =>
        (mockInternalQueue.appendElement _).expects(InputTuple(i))
      }
      (mockInternalQueue.appendElement _).expects(EndMarker())
      (mockInternalQueue.appendElement _).expects(EndOfAllMarker())
    }
    tupleProducer.registerInput(Identifier.None,0)
    tupleProducer.processDataEvents(Identifier.None, Iterable(inputBatch))
    tupleProducer.processDataEvents(Identifier.None, Iterable(EndOfUpstream()))
  }


  "tuple producer" should "be aware of upstream change" in{
    val tupleProducer = wire[TupleProducer]
    val inputBatchFromUpstream1 = DataPayload(Array.fill(4)(ITuple(1,2,3,5,"9.8",7.6)))
    val inputBatchFromUpstream2 = DataPayload(Array.fill(4)(ITuple(2,3,4,5,"6.7",8.9)))
    inSequence{
      (mockInternalQueue.appendElement _).expects(SenderChangeMarker(0))
      inputBatchFromUpstream1.payload.foreach{
        i =>
          (mockInternalQueue.appendElement _).expects(InputTuple(i))
      }
      (mockInternalQueue.appendElement _).expects(SenderChangeMarker(1))
      inputBatchFromUpstream2.payload.foreach{
        i =>
          (mockInternalQueue.appendElement _).expects(InputTuple(i))
      }
      (mockInternalQueue.appendElement _).expects(EndMarker())
      (mockInternalQueue.appendElement _).expects(SenderChangeMarker(0))
      (mockInternalQueue.appendElement _).expects(EndMarker())
      (mockInternalQueue.appendElement _).expects(EndOfAllMarker())
    }
    val first = ActorIdentifier("first upstream")
    val second = ActorIdentifier("second upstream")
    tupleProducer.registerInput(first,0)
    tupleProducer.registerInput(second,1)
    tupleProducer.processDataEvents(first, Iterable(inputBatchFromUpstream1))
    tupleProducer.processDataEvents(second, Iterable(inputBatchFromUpstream2, EndOfUpstream()))
    tupleProducer.processDataEvents(first, Iterable(EndOfUpstream()))

  }

}
