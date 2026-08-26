package com.learning.jdk8;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StreamsDemoTest {

    private final StreamsDemo demo = new StreamsDemo();

    @Test
    void filtersMapsAndSortsNames() {
        List<String> names = Arrays.asList("Al", "Bob", "Carl", "Dave", "Eve");
        assertEquals(Arrays.asList("AL", "CARL", "DAVE"), demo.upperCasedEvenLengthNames(names));
    }

    @Test
    void sumsLengths() {
        assertEquals(11, demo.sumOfLengths(Arrays.asList("Ann", "Bob", "Carla")));
    }
}
