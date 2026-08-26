package com.learning.jdk11;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringMethodsDemoTest {

    private final StringMethodsDemo demo = new StringMethodsDemo();

    @Test
    void stripsAndDropsBlankLines() {
        String text = "  first  \n\n   \n  second\n";
        assertEquals(List.of("first", "second"), demo.nonBlankTrimmedLines(text));
    }

    @Test
    void repeatsSymbol() {
        assertEquals("=====", demo.divider('=', 5));
    }
}
