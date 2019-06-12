package com.thalesgroup.datastorage.dojo;

import org.apache.kafka.clients.producer.MockProducer;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class UserGeneratorTest {

    @Test
    public void generateTest() {
        // Given
        MockProducer<String, String> mockProducer = new MockProducer<>();

        UserGenerator ug = Mockito.spy(UserGenerator.class);

        Mockito.when(ug.producer()).thenReturn(mockProducer);

        // When
        ug.generate(5);

        // Then
        assertEquals(5, mockProducer.history().size());
    }
}
