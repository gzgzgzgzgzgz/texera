package edu.uci.ics.amber.engine.architecture.messaginglayer

import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.messaginglayer.DataInputPort.WorkflowDataMessage
import edu.uci.ics.amber.engine.common.ambermessage.WorkerMessage.DataFrame
import edu.uci.ics.amber.engine.common.ambertag.neo.VirtualIdentity.NamedActorVirtualIdentity
import edu.uci.ics.amber.engine.common.tuple.ITuple

class DataInputPortSpec extends AnyFlatSpec with MockFactory {

  private val mockBatchToTupleConverter = mock[BatchToTupleConverter]
  private val fakeID = NamedActorVirtualIdentity("testReceiver")

  "data input port" should "output data in FIFO order" in {
    val inputPort = wire[DataInputPort]
    val payloads = (0 until 4).map { i =>
      DataFrame(Array(ITuple(i)))
    }.toArray
    val messages = (0 until 4).map { i =>
      WorkflowDataMessage(fakeID, i, payloads(i))
    }.toArray
    inSequence {
      (mockBatchToTupleConverter.processDataEvents _)
        .expects(fakeID, payloads.slice(0, 3).toIterable)
      (mockBatchToTupleConverter.processDataEvents _).expects(fakeID, Iterable(payloads(3)))
    }

    inputPort.handleDataMessage(messages(2))
    inputPort.handleDataMessage(messages(1))
    inputPort.handleDataMessage(messages(0))
    inputPort.handleDataMessage(messages(3))
  }

  "data input port" should "de-duplicate data " in {
    val inputPort = wire[DataInputPort]
    val payload = DataFrame(Array(ITuple(0)))
    val message = WorkflowDataMessage(fakeID, 0, payload)
    inSequence {
      (mockBatchToTupleConverter.processDataEvents _).expects(fakeID, Iterable(payload))
      (mockBatchToTupleConverter.processDataEvents _).expects(*, *).never
    }
    inputPort.handleDataMessage(message)
    inputPort.handleDataMessage(message)
    inputPort.handleDataMessage(message)
    inputPort.handleDataMessage(message)
    inputPort.handleDataMessage(message)
  }

}