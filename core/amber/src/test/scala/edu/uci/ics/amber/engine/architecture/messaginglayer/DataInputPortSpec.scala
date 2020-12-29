package edu.uci.ics.amber.engine.architecture.messaginglayer

import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.messaginglayer.DataInputPort.InternalDataMessage
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.DataPayload
import edu.uci.ics.amber.engine.common.ambertag.neo.Identifier
import edu.uci.ics.amber.engine.common.tuple.ITuple

class DataInputPortSpec extends AnyFlatSpec with MockFactory {

  private val mockTupleProducer = mock[TupleProducer]

  "data input port" should "output data in FIFO order" in {
    val inputPort = wire[DataInputPort]
    val payloads = (0 until 4).map{
      i => DataPayload(Array(ITuple(i)))
    }.toArray
    val messages = (0 until 4).map{
      i => InternalDataMessage(Identifier.None,i,payloads(i))
    }.toArray
    inSequence{
      (mockTupleProducer.processDataEvents _).expects(Identifier.None,payloads.slice(0,3).toIterable)
      (mockTupleProducer.processDataEvents _).expects(Identifier.None,Iterable(payloads(3)))
    }

    inputPort.handleDataMessage(messages(2))
    inputPort.handleDataMessage(messages(1))
    inputPort.handleDataMessage(messages(0))
    inputPort.handleDataMessage(messages(3))
  }

  "data input port" should "de-duplicate data " in {
    val inputPort = wire[DataInputPort]
    val payload = DataPayload(Array(ITuple(0)))
    val message = InternalDataMessage(Identifier.None,0,payload)
    inSequence{
      (mockTupleProducer.processDataEvents _).expects(Identifier.None,Iterable(payload))
      (mockTupleProducer.processDataEvents _).expects(*,*).never
    }
    inputPort.handleDataMessage(message)
    inputPort.handleDataMessage(message)
    inputPort.handleDataMessage(message)
    inputPort.handleDataMessage(message)
    inputPort.handleDataMessage(message)
  }

}
