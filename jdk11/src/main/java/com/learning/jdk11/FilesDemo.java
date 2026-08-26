package com.learning.jdk11;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JDK 11: Files.readString/writeString for simple whole-file text I/O without
 * manually wiring up a Reader/Writer or Charset-decoding stream.
 */
public class FilesDemo {

    public void writeThenRead(Path file, String content) throws IOException {
        Files.writeString(file, content);
        String roundTripped = Files.readString(file);
        if (!content.equals(roundTripped)) {
            throw new IllegalStateException("round trip mismatch");
        }
    }

    public long wordCount(Path file) throws IOException {
        // var (JDK 10): local-variable type inference for the obvious right-hand-side type.
        var text = Files.readString(file);
        return text.isBlank() ? 0 : text.trim().split("\\s+").length;
    }

    public static void main(String[] args) throws IOException {
        Path tempFile = Files.createTempFile("jdk11-files-demo", ".txt");
        try {
            basicRoundTrip(tempFile);
            appendingAndOptions(tempFile);
            explicitCharset(tempFile);
            wordCountDemo(tempFile);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void basicRoundTrip(Path file) throws IOException {
        // Before 11: new String(Files.readAllBytes(file), StandardCharsets.UTF_8) -- readString replaces that.
        Files.writeString(file, "Hello, JDK 11 Files API!");
        String content = Files.readString(file);
        System.out.println("Files.writeString/readString round trip: " + content);
    }

    private static void appendingAndOptions(Path file) throws IOException {
        Files.writeString(file, "first line\n");
        Files.writeString(file, "second line\n", java.nio.file.StandardOpenOption.APPEND);
        System.out.println("after writeString(..., APPEND):\n" + Files.readString(file));
    }

    private static void explicitCharset(Path file) throws IOException {
        // Both methods default to UTF-8 but accept any Charset explicitly.
        String withEmoji = "cafe with umlaut: café";
        Files.writeString(file, withEmoji, StandardCharsets.ISO_8859_1);
        String readBack = Files.readString(file, StandardCharsets.ISO_8859_1);
        System.out.println("round trip through ISO-8859-1 explicitly: " + readBack);
    }

    private static void wordCountDemo(Path file) throws IOException {
        Files.writeString(file, "the quick brown fox jumps over the lazy dog");
        FilesDemo demo = new FilesDemo();
        System.out.println("wordCount(...) using var + readString: " + demo.wordCount(file));
    }
}
