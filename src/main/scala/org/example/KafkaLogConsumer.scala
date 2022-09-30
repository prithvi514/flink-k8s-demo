package org.example

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.functions.FilterFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

import scala.util.matching.Regex


//class BadReqFilter extends FilterFunction[String] {
//  override def filter(s: String): Boolean = {
//    var returnFlag: Boolean = false
//    val logPattern: Regex = """^([\d.]+) (\S+) (\S+) \[(.*)\] \"([^\s]+) (/[^\s]*) HTTP/[^\s]+\" (\d{3}) (\d+) \"([^\"]+)\" \"([^\"]+)\" (\S+)""".r
//    for (patternMatch <- logPattern.findAllMatchIn(s))
//      if(patternMatch.group(7) != 200){
//        returnFlag = true
//      }
//    returnFlag
//  }
//}

object KafkaLogConsumer {

  def main(args: Array[String]) {

    // set up the streaming execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val kafkaProperties = Map(
      "zookeeper.connect" -> "localhost:2181",
      "group.id" -> "flink",
      "bootstrap.servers" -> "kafka-svc:9092",
      "source.topic" -> "logs-source",
      "sink.topic" -> "logs-sink"
    )

    val producer = KafkaSink.builder[String]()
      .setBootstrapServers(kafkaProperties("bootstrap.servers"))
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic(kafkaProperties("sink.topic"))
          .setValueSerializationSchema(new SimpleStringSchema())
          .build())
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE).build()

    val consumer = KafkaSource.builder[String]()
      .setBootstrapServers(kafkaProperties("bootstrap.servers"))
      .setTopics(kafkaProperties("source.topic"))
      .setGroupId(kafkaProperties("group.id"))
      .setStartingOffsets(OffsetsInitializer.earliest())
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()


    val stringInputStream: DataStream[String] = env.fromSource(consumer, WatermarkStrategy.noWatermarks(), "Flink-Log-Consumer")

    //val events = stringInputStream.filter(new BadReqFilter())

    val events = stringInputStream.filter(new FilterFunction[String]() {
        override def filter(value: String): Boolean = {
          var returnFlag: Boolean = false
          val logPattern: Regex = """^([\d.]+) (\S+) (\S+) \[(.*)\] \"([^\s]+) (/[^\s]*) HTTP/[^\s]+\" (\d{3}) (\d+) \"([^\"]+)\" \"([^\"]+)\" (\S+)""".r
          // ToDo: Filter not working as expected
          for (patternMatch <- logPattern.findAllMatchIn(value))
            if (patternMatch.group(7) != 200) {
              returnFlag = true
            }
          returnFlag
        }
      }
    )

    events.sinkTo(producer)

    env.execute("Flink Log Consumer")

  }
}
