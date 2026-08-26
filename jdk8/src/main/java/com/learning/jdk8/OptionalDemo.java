package com.learning.jdk8;

import java.util.List;
import java.util.Optional;

/**
 * JDK 8: java.util.Optional models "value or absent" explicitly in the type
 * system, as an alternative to returning null.
 */
public class OptionalDemo {

    public Optional<String> firstLongerThan(List<String> names, int minLength) {
        return names.stream()
                .filter(name -> name.length() > minLength)
                .findFirst();
    }

    public String describe(Optional<String> maybeName) {
        return maybeName.map(name -> "found: " + name).orElse("nothing found");
    }

    public static void main(String[] args) {
        creating();
        readingSafely();
        mapFilterFlatMap();
        orElseVariants();
    }

    private static void creating() {
        Optional<String> present = Optional.of("value");           // throws NPE if the argument is null
        Optional<String> absent = Optional.empty();                // explicitly empty
        Optional<String> maybeNull = Optional.ofNullable(null);    // null-safe: becomes empty()

        System.out.println("Optional.of(\"value\"): " + present);
        System.out.println("Optional.empty(): " + absent);
        System.out.println("Optional.ofNullable(null): " + maybeNull);
    }

    private static void readingSafely() {
        Optional<String> present = Optional.of("42");
        Optional<String> absent = Optional.empty();

        System.out.println("present.isPresent(): " + present.isPresent());
        System.out.println("absent.isPresent(): " + absent.isPresent());
        System.out.println("absent.isEmpty()... not available until JDK 11, use !isPresent(): " + !absent.isPresent());

        present.ifPresent(v -> System.out.println("ifPresent ran with: " + v));
        absent.ifPresent(v -> System.out.println("this will never print"));
    }

    private static void mapFilterFlatMap() {
        Optional<String> raw = Optional.of("  42  ");

        Optional<Integer> parsed = raw
                .map(String::trim)                       // Optional<String> -> Optional<String>
                .filter(s -> s.chars().allMatch(Character::isDigit)) // keep only if all digits
                .map(Integer::parseInt);                 // Optional<String> -> Optional<Integer>

        System.out.println("map(trim).filter(isDigits).map(parseInt) on \"  42  \": " + parsed);

        Optional<String> notNumeric = Optional.of("  abc  ")
                .map(String::trim)
                .filter(s -> s.chars().allMatch(Character::isDigit));
        System.out.println("same pipeline on \"  abc  \" (fails filter): " + notNumeric);

        // flatMap avoids Optional<Optional<T>> when the mapper itself returns an Optional.
        Optional<Optional<String>> nested = Optional.of("x").map(OptionalDemo::maybeUpper);
        Optional<String> flat = Optional.of("x").flatMap(OptionalDemo::maybeUpper);
        System.out.println("map() with an Optional-returning function nests: " + nested);
        System.out.println("flatMap() with the same function flattens: " + flat);
    }

    private static Optional<String> maybeUpper(String s) {
        return s.isEmpty() ? Optional.empty() : Optional.of(s.toUpperCase());
    }

    private static void orElseVariants() {
        Optional<String> absent = Optional.empty();

        System.out.println("orElse(\"default\"): " + absent.orElse("default"));
        System.out.println("orElseGet(supplier): " + absent.orElseGet(() -> "computed default"));
        try {
            absent.orElseThrow(() -> new IllegalStateException("nothing here"));
        } catch (IllegalStateException e) {
            System.out.println("orElseThrow(...) raised: " + e.getMessage());
        }

        // A common pitfall: orElse(...) always evaluates its argument, even when the Optional is
        // present, because Java evaluates method arguments eagerly. Prefer orElseGet for anything
        // expensive so it's only computed on the empty path.
        Optional<String> present = Optional.of("already here");
        String result = present.orElse(expensiveFallback());
        System.out.println("present.orElse(expensiveFallback()) still called the fallback: " + result);
    }

    private static String expensiveFallback() {
        System.out.println("  (expensiveFallback() was evaluated eagerly)");
        return "fallback";
    }
}
