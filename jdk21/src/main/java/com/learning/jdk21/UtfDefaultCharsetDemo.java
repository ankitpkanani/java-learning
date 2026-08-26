package com.learning.jdk21;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * JDK 18 (JEP 400): UTF-8 by default. Before 18, APIs like new String(bytes),
 * new FileReader(file), and Charset.defaultCharset() silently used the
 * platform's default charset -- which could be UTF-8 on one machine and,
 * historically, something like windows-1252 on another. That made "works on
 * my machine" charset bugs common. JDK 18 makes UTF-8 the standard default
 * everywhere, regardless of OS/locale settings.
 */
public class UtfDefaultCharsetDemo {

    public static void main(String[] args) {
        defaultCharsetIsUtf8();
        implicitlyUtf8Apis();
    }

    private static void defaultCharsetIsUtf8() {
        // Since JDK 18, this is guaranteed to be UTF-8 -- not "whatever the OS happens to be set to".
        Charset defaultCharset = Charset.defaultCharset();
        System.out.println("Charset.defaultCharset(): " + defaultCharset);
        System.out.println("equals StandardCharsets.UTF_8: " + defaultCharset.equals(StandardCharsets.UTF_8));
        System.out.println("file.encoding system property: " + System.getProperty("file.encoding"));
    }

    private static void implicitlyUtf8Apis() {
        // new String(byte[]) with no explicit Charset argument used the platform default before 18;
        // now it's UTF-8 unconditionally. Encode some non-ASCII text and decode it back with no
        // charset specified anywhere -- this only round-trips correctly because both sides agree
        // it's UTF-8 by default.
        String original = "café ☃ 日本語"; // "café ☃ 日本語"
        byte[] utf8Bytes = original.getBytes(StandardCharsets.UTF_8);
        String decodedWithNoExplicitCharset = new String(utf8Bytes); // relies on the JDK 18+ default

        System.out.println("original: " + original);
        System.out.println("new String(bytes) with no Charset arg round-trips correctly: "
                + decodedWithNoExplicitCharset.equals(original));

        // PrintStream(OutputStream) is the same story: System.out itself now defaults to UTF-8.
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream printStream = new PrintStream(buffer)) { // no charset argument
            printStream.print(original);
        }
        System.out.println("PrintStream with no Charset arg writes UTF-8 bytes: "
                + buffer.toString(StandardCharsets.UTF_8).equals(original));
    }
}
