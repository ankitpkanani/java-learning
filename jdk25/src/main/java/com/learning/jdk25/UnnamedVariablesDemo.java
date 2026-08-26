package com.learning.jdk25;

import java.util.List;
import java.util.Map;

/**
 * JDK 22 (JEP 456, finalized -- carried forward into 25): unnamed variables
 * and patterns. "_" marks a variable, lambda parameter, catch parameter, or
 * pattern component you're required to declare but never actually use --
 * documenting "deliberately ignored" instead of forcing a throwaway name
 * (or worse, an unused-but-real name the compiler/linter then warns about).
 */
public class UnnamedVariablesDemo {

    sealed interface Shape permits Circle, Square {}
    record Circle(double radius) implements Shape {}
    record Square(double side) implements Shape {}

    public static void main(String[] args) {
        unusedCatchParameter();
        unusedEnhancedForVariable();
        unusedLambdaParameter();
        unusedRecordPatternComponent();
    }

    private static void unusedCatchParameter() {
        try {
            Integer.parseInt("not a number");
        } catch (NumberFormatException _) {
            // We only care THAT parsing failed, never the exception's own details.
            System.out.println("catch (NumberFormatException _): caught, exception object itself unused");
        }
    }

    private static void unusedEnhancedForVariable() {
        List<String> items = List.of("a", "b", "c", "d");
        int count = 0;
        // We only need the loop to run once per element -- the element's value is irrelevant here.
        for (String _ : items) {
            count++;
        }
        System.out.println("for (String _ : items) just counting: " + count);
    }

    private static void unusedLambdaParameter() {
        Map<String, Integer> scores = Map.of("alice", 90, "bob", 85);
        // forEach hands us both key and value, but this particular call only needs the keys.
        scores.forEach((name, _) -> System.out.println("  visited key: " + name));
    }

    private static void unusedRecordPatternComponent() {
        List<Shape> shapes = List.of(new Circle(2), new Square(3), new Circle(5));
        long circleCount = shapes.stream()
                // The radius value itself doesn't matter for a count -- only that it WAS a Circle.
                .filter(shape -> shape instanceof Circle(double _))
                .count();
        System.out.println("counting Circles via 'instanceof Circle(double _)': " + circleCount);
    }
}
