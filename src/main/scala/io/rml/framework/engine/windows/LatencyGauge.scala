package io.rml.framework.engine.windows

import io.rml.framework.flink.item.JoinedItem
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala.metrics.ScalaGauge
import org.apache.flink.configuration.Configuration


class LatencyGauge extends RichMapFunction[Iterable[JoinedItem], Iterable[JoinedItem]]{
  @transient private var latency:Long = 0
  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    getRuntimeContext()
      .getMetricGroup()
      .gauge[Long, ScalaGauge[Long]]("Latency", ScalaGauge[Long]( () => latency ) )
  }
  override def map(in: Iterable[JoinedItem]): Iterable[JoinedItem] = {
    val now = System.currentTimeMillis()
    val joinedItem = in.head
    val childTime = joinedItem.child.refer("latency").get.head.toLong
    val parentTime= joinedItem.parent.refer("latency").get.head.toLong
    val childLatency = now - childTime
    val parentlatency = now - parentTime
    latency = if (childLatency < parentlatency) childLatency else parentlatency

    in
  }
}
