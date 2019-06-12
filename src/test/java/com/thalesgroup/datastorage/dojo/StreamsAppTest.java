package com.thalesgroup.datastorage.dojo;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.Test;

import java.util.Properties;

public class StreamsAppTest {

    @Test
    public void createTopology() {
        // Given
        Topology topology = StreamsApp.createTopology();
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // When
        ProducerRecord<String, String> producerRecord;
        try (TopologyTestDriver driver = new TopologyTestDriver(topology, props);
             Serde<String> serde = new Serdes.StringSerde()
        ) {

            ConsumerRecordFactory<String, String> eventsFactory = new ConsumerRecordFactory<>("events", serde.serializer(), serde.serializer());
            ConsumerRecordFactory<String, String> usersFactory = new ConsumerRecordFactory<>("users", serde.serializer(), serde.serializer());

            driver.pipeInput(usersFactory.create("users", "key_0", "user"));
            driver.pipeInput(eventsFactory.create("events", "key_0", "event"));

            producerRecord = driver.readOutput("output", serde.deserializer(), serde.deserializer());
        }

        // Then
        OutputVerifier.compareValue(producerRecord, "User user sent event");
    }
}
