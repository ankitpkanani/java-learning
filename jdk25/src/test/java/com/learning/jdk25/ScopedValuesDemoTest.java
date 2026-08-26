package com.learning.jdk25;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScopedValuesDemoTest {

    private final ScopedValuesDemo demo = new ScopedValuesDemo();

    @Test
    void bindsRequestIdWithinScope() throws Exception {
        assertEquals("none", demo.currentRequestId());
        String result = demo.runWithRequestId("req-42", demo::currentRequestId);
        assertEquals("req-42", result);
    }
}
