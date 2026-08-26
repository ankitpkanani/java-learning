package com.learning.jdk21;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PatternMatchingSwitchTest {

    private final PatternMatchingSwitch demo = new PatternMatchingSwitch();

    @Test
    void computesAreaViaRecordPatterns() {
        var circle = new PatternMatchingSwitch.Circle(new PatternMatchingSwitch.Point(0, 0), 2);
        assertEquals(Math.PI * 4, demo.area(circle), 1e-9);
    }

    @Test
    void describesValuesByType() {
        assertEquals("negative int", demo.describe(-1));
        assertEquals("int: 5", demo.describe(5));
        assertEquals("null value", demo.describe(null));
    }
}
