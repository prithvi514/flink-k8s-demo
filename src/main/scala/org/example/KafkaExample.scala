package org.example

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment


class WordsCapitalizer extends MapFunction[String, String] {
  override def map(s: String): String = s.toUpperCase
}

object KafkaExample {
  def main(args: Array[String]) {

    // set up the streaming execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val kafkaProperties = Map(
      "zookeeper.connect" -> "localhost:2181",
      "group.id" -> "flink",
      "bootstrap.servers" -> "localhost:9092",
      "source.topic" -> "test-source",
      "sink.topic" -> "test-sink"
    )

    val producer = KafkaSink.builder[String]()
      .setBootstrapServers(kafkaProperties("brokers"))
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic(kafkaProperties("source.topic"))
          .setValueSerializationSchema(new SimpleStringSchema())
          .build())
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE).build()

    val consumer = KafkaSource.builder[String]()
      .setBootstrapServers(kafkaProperties("brokers"))
      .setTopics("sink.topic")
      .setGroupId(kafkaProperties("group.id"))
      .setStartingOffsets(OffsetsInitializer.latest())
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()



    val stringInputStream: DataStream[String] = env.fromSource(consumer,WatermarkStrategy.noWatermarks(),"Flink-Consumer")

    val events = stringInputStream.map(new WordsCapitalizer())//.addSink(producer)

    events.sinkTo(producer)



//    val stringInputStream = new DataStream[String]()
//
//
//    val stringInputStream:DataStream[String] = env.addSource(consumer)
//    stringInputStream.map(new WordsCapitalizer()).addSink(producer)
//    stringInputStream.addSink(producer)

    //env.fromSource(producer, WatermarkStrategy.noWatermarks(), "Kafka-Flink-Source")
    //env.execute("Socket Window WordCount")


  }
}
