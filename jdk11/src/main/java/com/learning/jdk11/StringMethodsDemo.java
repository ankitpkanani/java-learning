package com.learning.jdk11;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JDK 11: new String methods -- isBlank, strip/stripLeading/stripTrailing
 * (Unicode-aware, unlike trim), lines, and repeat.
 */
public class StringMethodsDemo {

    public List<String> nonBlankTrimmedLines(String text) {
        return text.lines()
                .map(String::strip)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
    }

    public String divider(char symbol, int width) {
        return String.valueOf(symbol).repeat(width);
    }

    public static void main(String[] args) {
        isBlankVsIsEmpty();
        stripVsTrim();
        linesDemo();
        repeatDemo();
    }

    private static void isBlankVsIsEmpty() {
        String empty = "";
        String whitespaceOnly = "   \t  ";
        String withText = "  hi  ";

        // isBlank() (11) generalizes isEmpty(): true for "" AND for whitespace-only strings.
        System.out.println("\"\".isEmpty() / isBlank(): " + empty.isEmpty() + " / " + empty.isBlank());
        System.out.println("\"   \\t  \".isEmpty() / isBlank(): " + whitespaceOnly.isEmpty() + " / " + whitespaceOnly.isBlank());
        System.out.println("\"  hi  \".isEmpty() / isBlank(): " + withText.isEmpty() + " / " + withText.isBlank());
    }

    private static void stripVsTrim() {
        // U+2003 EM SPACE is a genuine Unicode whitespace character (Character.isWhitespace ==
        // true), but it is > U+0020, so legacy trim() -- which only strips chars <= U+0020 --
        // leaves it behind. strip() uses Character.isWhitespace() and removes it correctly.
        //
        // Note U+00A0 NO-BREAK SPACE is deliberately NOT considered whitespace by Java at all
        // (Character.isWhitespace explicitly excludes non-breaking spaces), so neither trim()
        // nor strip() touches it -- that is a common misconception about this pair of methods.
        String withUnicodeSpace = "\u2003\u2003padded\u2003\u2003";

        System.out.println("trim() on an EM-SPACE-padded string leaves them behind (length "
                + withUnicodeSpace.trim().length() + ", expected 10): [" + withUnicodeSpace.trim() + "]");
        System.out.println("strip() correctly removes Unicode whitespace (length "
                + withUnicodeSpace.strip().length() + ", expected 6): [" + withUnicodeSpace.strip() + "]");

        String leftRightPadded = "   both sides   ";
        System.out.println("stripLeading(): [" + leftRightPadded.stripLeading() + "]");
        System.out.println("stripTrailing(): [" + leftRightPadded.stripTrailing() + "]");
    }

    private static void linesDemo() {
        String multiline = "first\nsecond\r\nthird\n   \nfourth";

        // lines() splits on \n, \r, or \r\n and hands back a Stream<String> -- no manual split()/regex.
        System.out.println("String.lines() splits on \\n, \\r, \\r\\n:");
        multiline.lines().forEach(line -> System.out.println("  [" + line + "]"));

        StringMethodsDemo demo = new StringMethodsDemo();
        System.out.println("nonBlankTrimmedLines(...): " + demo.nonBlankTrimmedLines(multiline));
    }

    private static void repeatDemo() {
        System.out.println("\"ab\".repeat(3): " + "ab".repeat(3));
        System.out.println("\"=\".repeat(0): [" + "=".repeat(0) + "] (empty string, not an error)");

        StringMethodsDemo demo = new StringMethodsDemo();
        System.out.println("divider('-', 20): " + demo.divider('-', 20));
    }
}
