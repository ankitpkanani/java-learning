package com.learning.jdk11;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FilesDemoTest {

    private final FilesDemo demo = new FilesDemo();

    @Test
    void writesAndReadsBackSameContent(@TempDir Path tempDir) {
        Path file = tempDir.resolve("notes.txt");
        assertDoesNotThrow(() -> demo.writeThenRead(file, "hello jdk 11"));
    }

    @Test
    void countsWords(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("notes.txt");
        demo.writeThenRead(file, "the quick brown fox");
        assertEquals(4, demo.wordCount(file));
    }
}
