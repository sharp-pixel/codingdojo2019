package com.thalesgroup.datastorage.dojo;

import com.thalesgroup.datastorage.dojo.listeners.ConsoleGlobalRestoreListener;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;
import java.util.Properties;

@Slf4j
public class StreamsApp {

    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";
    private static final String BOOTSTRAP_SERVERS = "PLAINTEXT://localhost:9092";

    public static void main(String[] args) {
        Properties props = createProperties();
        Topology topology = createTopology();
        final KafkaStreams streams = new KafkaStreams(topology, props);

        System.out.println(topology.describe().toString());

        streams.setGlobalStateRestoreListener(new ConsoleGlobalRestoreListener());

        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                log.trace("Closing Kafka Streams app");
                streams.close();
            }
        });

        streams.start();
    }

    private static Properties createProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once");

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);

        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest");
        props.put(StreamsConfig.producerPrefix(ProducerConfig.RETRIES_CONFIG), Integer.MAX_VALUE);
        props.put(StreamsConfig.producerPrefix(ProducerConfig.MAX_BLOCK_MS_CONFIG), Integer.MAX_VALUE);
        props.put(StreamsConfig.REQUEST_TIMEOUT_MS_CONFIG, 305000);
        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG), Integer.MAX_VALUE);

        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        props.put(StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG, DefaultProductionExceptionHandler.class);

        return props;
    }

    static Topology createTopology() {
        final StreamsBuilder builder = new StreamsBuilder();

        final Serdes.StringSerde stringSerde = new Serdes.StringSerde();
        final KStream<String, String> users = builder.stream("users", Consumed.with(stringSerde, stringSerde));
        final KStream<String, String> events = builder.stream("events", Consumed.with(stringSerde, stringSerde));

        final KStream<String, String> joinedStream = events.join(
                users,
                (event, user) -> "User " + user + " sent " + event,
                JoinWindows.of(Duration.ofSeconds(5)),
                Joined.with(stringSerde, stringSerde, stringSerde)
        );

        joinedStream.to("output", Produced.with(stringSerde, stringSerde));

        return builder.build();
    }

    static Topology createTopology2() {
        final StreamsBuilder builder = new StreamsBuilder();

        final Serdes.StringSerde stringSerde = new Serdes.StringSerde();
        final KTable<String, String> users = builder.table("users", Consumed.with(stringSerde, stringSerde), Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("events").withKeySerde(stringSerde).withValueSerde(stringSerde));
        final KStream<String, String> events = builder.stream("events", Consumed.with(stringSerde, stringSerde));

        final KStream<String, String> joinedStream = events.leftJoin(
                users,
                (event, user) -> "User " + user + " sent " + event,
                Joined.with(stringSerde, stringSerde, stringSerde)
        );

        joinedStream.to("output", Produced.with(stringSerde, stringSerde));

        return builder.build();
    }
}
