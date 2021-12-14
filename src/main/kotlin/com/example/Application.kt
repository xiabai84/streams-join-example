package com.example

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.JoinWindows
import org.apache.kafka.streams.kstream.Printed
import java.time.Duration
import java.util.*

fun main() {

    val bootstrapServersConfig = System.getenv("BOOTSTRAP_SERVERS_CONFIG") ?: "127.0.0.1:9092"
    val applicationIdConfig = System.getenv("APPLICATION_ID_CONFIG") ?: "kafka-streams-join-example"
    val props = Properties()

    with(props) {
        put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig)
        put(StreamsConfig.APPLICATION_ID_CONFIG, applicationIdConfig)
        put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().javaClass.name)
        put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass.name)
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    }

    val builder = StreamsBuilder()

    val left= builder.stream("topic-left", Consumed.with(Serdes.String(), Serdes.String()))
    val right= builder.stream("topic-right", Consumed.with(Serdes.String(), Serdes.String()))

    left.foreach{ key, value ->
        println("-> left: $key, $value")
    }

    right.foreach{ key, value ->
        println("-> right: $key, $value")
    }

    // inner join will join data with key from left and right sides
    left.join(
        right,
        { v1, v2 ->"streams-inner-join: left:$v1 - right:$v2" },
        JoinWindows.of(Duration.ofSeconds(30))
    )
    .also { it.print(Printed.toSysOut()) }


    // Join operations can produce multiple records, if multiple values with same key are present in the same join-window
    // This duplication could be removed by using aggreate function like reduce, aggregate or further low level APIs.
    left.join(
        right,
        { v1, v2 ->"streams-inner-join: left:$v1 - right:$v2" },
        JoinWindows.of(Duration.ofSeconds(30))
    )
    .groupByKey()
    .reduce{ _, v2 -> v2 } // only keep the last change during the join window
    .toStream()
    .also { it.print(Printed.toSysOut()) }

    // left join will produce null value in right-table, if key from right side is not present
    left.leftJoin(
        right,
        { v1, v2 -> "streams-left-join: left:$v1 - right:$v2" },
        JoinWindows.of(Duration.ofSeconds(30))
    )
    .also { it.print(Printed.toSysOut()) }

    val streams = KafkaStreams(builder.build(), props)

    streams.cleanUp()
    streams.start()
    streams.localThreadsMetadata().forEach { data -> println(data) }
    Runtime.getRuntime().addShutdownHook(Thread(streams::close))

}
