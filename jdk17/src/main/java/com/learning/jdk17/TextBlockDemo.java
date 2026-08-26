package com.learning.jdk17;

/**
 * JDK 15 (JEP 378): text blocks for multi-line string literals without
 * escaping every quote and newline.
 */
public class TextBlockDemo {

    public String json(String name, int age) {
        return """
                {
                  "name": "%s",
                  "age": %d
                }""".formatted(name, age);
    }

    public static void main(String[] args) {
        basicTextBlock();
        incidentalWhitespaceStripping();
        lineContinuationBackslash();
        explicitTrailingSpaceWithBackslashS();
        formattedJson();
    }

    private static void basicTextBlock() {
        // Triple-quote opens the block; embedded double quotes need no escaping at all.
        String html = """
                <html>
                  <body>
                    <p>Hello, "text blocks"!</p>
                  </body>
                </html>""";
        System.out.println("basic text block:\n" + html);
    }

    private static void incidentalWhitespaceStripping() {
        // The compiler strips "incidental" leading whitespace common to every line, based on the
        // least-indented line (including the closing delimiter's own line, if it's on its own line).
        // Indentation you actually want to KEEP must go deeper than that common minimum.
        String block = """
                one
                  two
                    three
                """;
        System.out.println("incidental whitespace stripped, relative indentation kept:");
        System.out.println("[" + block + "]");
    }

    private static void lineContinuationBackslash() {
        // A trailing backslash suppresses the line break that would otherwise be inserted --
        // it joins this line to the next one, useful for wrapping a long line in source without
        // adding an unwanted newline to the actual string value.
        String block = """
                This is one long logical line \
                that was wrapped across two source lines \
                but contains no embedded newlines.""";
        System.out.println("backslash line continuation:");
        System.out.println("[" + block + "]");
    }

    private static void explicitTrailingSpaceWithBackslashS() {
        // Editors/tools often strip trailing whitespace, which would otherwise be silently lost
        // from a text block. \s is an explicit single space that survives such tools untouched.
        String block = """
                trailing space ->\s
                next line""";
        System.out.println("\\s forces an explicit trailing space:");
        System.out.println("[" + block + "]");
    }

    private static void formattedJson() {
        TextBlockDemo demo = new TextBlockDemo();
        System.out.println("json(\"Ada\", 36):\n" + demo.json("Ada", 36));
    }
}
