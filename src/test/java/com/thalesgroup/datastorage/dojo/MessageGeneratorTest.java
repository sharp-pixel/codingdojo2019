package com.thalesgroup.datastorage.dojo;

import org.apache.kafka.clients.producer.MockProducer;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class MessageGeneratorTest {

    @Test
    public void generateSimple() {
        // Given
        MockProducer<String, String> mockProducer = new MockProducer<>();

        MessageGenerator ug = Mockito.spy(MessageGenerator.class);

        Mockito.when(ug.producer()).thenReturn(mockProducer);

        // When
        ug.generateSimple(5);

        // Then
        assertEquals(5, mockProducer.history().size());
    }

    @Test
    public void generateWithTransaction() {
        // Given
        MockProducer<String, String> mockProducer = new MockProducer<>();

        MessageGenerator ug = Mockito.spy(MessageGenerator.class);

        Mockito.when(ug.producer()).thenReturn(mockProducer);

        // When
        ug.generateWithTransaction(5);

        // Then
        assertEquals(5, mockProducer.history().size());
    }

    @Test
    public void generateWithTransactionReverse() {
        // Given
        MockProducer<String, String> mockProducer = new MockProducer<>();

        MessageGenerator ug = Mockito.spy(MessageGenerator.class);

        Mockito.when(ug.producer()).thenReturn(mockProducer);

        // When
        ug.generateWithTransactionReverse(5);

        // Then
        assertEquals(5, mockProducer.history().size());
    }
}
