package com.thalesgroup.datastorage.dojo;

import com.thalesgroup.datastorage.dojo.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;

import java.io.Closeable;
import java.util.UUID;

@Slf4j
public class MessageGenerator implements Closeable {
    private Producer<String, String> kafkaProducer;
    private static final String TOPIC_NAME = "events";

    Producer<String, String> producer() {
        if (kafkaProducer == null) {
            kafkaProducer = new KafkaProducer<>(KafkaConfig.getProducerConfig("messages"));
        }

        return kafkaProducer;
    }

    private void produceRecord(int i) {
        final Producer<String, String> producer = producer();
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_NAME, String.valueOf(i), UUID.randomUUID().toString());

        producer.send(producerRecord, (recordMetadata, e) -> {
            if (e != null) {
                String topic = recordMetadata == null ? "<unknown>" : recordMetadata.topic();
                log.error("Error when sending to topic {}", topic, e);
            }
        });
    }

    public void generateWithTransaction(int n) {
        final Producer<String, String> producer = producer();

        producer.initTransactions();
        try {
            producer.beginTransaction();
            for (int i = n - 1; i >= 0; i--) {
                produceRecord(i);
            }
            producer.commitTransaction();
        } catch (KafkaException e) {
            log.error("Could not send to topic {}", TOPIC_NAME, e);
            producer.abortTransaction();
        }
    }

    public void generateWithTransactionReverse(int n) {
        final Producer<String, String> producer = producer();

        producer.initTransactions();
        try {
            producer.beginTransaction();
            for (int i = n; i-- > 0; ) {
                produceRecord(i);
            }
            producer.commitTransaction();
        } catch (KafkaException e) {
            log.error("Could not send to topic {}", TOPIC_NAME, e);
            producer.abortTransaction();
        }
    }

    public void generateSimple(int n) {
        try {
            for (int i = n; i-- > 0; ) {
                produceRecord(i);
            }
        } catch (KafkaException e) {
            log.error("Could not send to topic {}", TOPIC_NAME, e);
        }
    }

    @Override
    public void close() {
        producer().close();
    }

    public static void main(String[] args) {

        long timeBefore;
        try (MessageGenerator ug = new MessageGenerator()) {
            log.trace("Starting message generation");
            timeBefore = System.nanoTime();
            ug.generateWithTransaction(100000);

            long timeAfter = System.nanoTime();

            long timePassed = timeAfter - timeBefore;
            long timePassedMs = timePassed / 1000000;
            log.info("Generation took {} ms", timePassedMs);
        }
    }
}
