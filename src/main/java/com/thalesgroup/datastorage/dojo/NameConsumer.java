package com.thalesgroup.datastorage.dojo;

import com.thalesgroup.datastorage.dojo.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.Closeable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class NameConsumer implements Closeable, Runnable {
    private Consumer<String, String> kafkaConsumer;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private Map<String, String> users = new HashMap<>();

    Consumer<String, String> consumer() {
        if (kafkaConsumer == null) {
            kafkaConsumer = new KafkaConsumer<>(KafkaConfig.getConsumerConfig());
        }
        return kafkaConsumer;
    }

    void consumeUsers() {
        running.set(true);

        final Consumer<String, String> consumer = consumer();

        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record);
                }

                consumer.commitAsync();
            }
        } catch (Exception e) {
            log.error("Error while polling topic", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }

    void processRecord(ConsumerRecord<String, String> record) {
        users.put(record.key(), record.value());
    }

    @Override
    public void close() {
        consumer().close();
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }

    public Map<String, String> getUsers() {
        return this.users;
    }

    public void start() {
        Thread worker = new Thread(this);
        worker.start();
    }

    public void stop() {
        running.set(false);
    }

    @Override
    public void run() {
        consumeUsers();
    }
}
