package com.thalesgroup.datastorage.dojo;

import com.thalesgroup.datastorage.dojo.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;

import java.io.Closeable;

@Slf4j
public class UserGenerator implements Closeable {
    private Producer<String, String> kafkaProducer;
    private static final String TOPIC_NAME = "users";

    Producer<String, String> producer() {
        if (kafkaProducer == null) {
            kafkaProducer = new KafkaProducer<>(KafkaConfig.getProducerConfig("users"));
        }
        return kafkaProducer;
    }

    public void generate(int n) {
        final Producer<String, String> producer = producer();

        producer.initTransactions();

        try {
            producer.beginTransaction();
            for (int i = 0; i < n; i++) {
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_NAME, String.valueOf(i), NameGenerator.getName());

                producer.send(producerRecord, (recordMetadata, e) -> {
                    if (e != null) {
                        log.error("Error with topic {}", recordMetadata.topic(), e);
                    }
                });
            }
            producer.commitTransaction();
        } catch (KafkaException e) {
            producer.abortTransaction();
        }
    }

    @Override
    public void close() {
        producer().close();
    }

    public static void main(String[] args) {
        long timeBefore;

        try (UserGenerator ug = new UserGenerator()) {
            timeBefore = System.nanoTime();
            ug.generate(1000);
        }
        long timeAfter = System.nanoTime();

        long timePassed = timeAfter - timeBefore;
        long timePassedMs = timePassed / 1000000;
        log.info("Generation took {} ms", timePassedMs);
    }
}
