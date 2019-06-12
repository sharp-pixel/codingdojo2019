package com.thalesgroup.datastorage.dojo;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;

public class NameConsumerTest {

    public static final String TOPIC = "Users";

    @Test
    public void consumeUsers() {
        MockConsumer<String, String> mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);

        TopicPartition topicPartition = new TopicPartition(TOPIC, 0);
        mockConsumer.assign(Collections.singletonList(topicPartition));

        Map<TopicPartition, Long> topics = new HashMap<>();
        topics.put(topicPartition, 0L);
        mockConsumer.updateBeginningOffsets(topics);

        ConsumerRecord<String, String> rec1 = new ConsumerRecord<>(TOPIC, 0, 0L, "0", NameGenerator.getName());
        ConsumerRecord<String, String> rec2 = new ConsumerRecord<>(TOPIC, 0, 1L, "1", NameGenerator.getName());
        ConsumerRecord<String, String> rec3 = new ConsumerRecord<>(TOPIC, 0, 2L, "2", NameGenerator.getName());
        mockConsumer.addRecord(rec1);
        mockConsumer.addRecord(rec2);
        mockConsumer.addRecord(rec3);

        NameConsumer nameConsumer = Mockito.spy(NameConsumer.class);

        Mockito.when(nameConsumer.consumer()).thenReturn(mockConsumer);

        nameConsumer.start();
        await().atMost(1, TimeUnit.SECONDS).until(() ->
                nameConsumer.getUsers().size(), equalTo(3)
        );
        nameConsumer.stop();
    }
}
