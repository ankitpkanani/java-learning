package com.learning.jdk11;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * JDK 11: a handful of small-but-handy API additions that don't get their own
 * JEP headline but show up constantly once you know them.
 */
public class NewApiAdditionsDemo {

    public static void main(String[] args) {
        predicateNot();
        optionalIsEmpty();
        collectionToArrayWithGenerator();
        patternAsMatchPredicate();
    }

    private static void predicateNot() {
        List<String> words = Arrays.asList("", "hello", "  ", "world", "");

        // Before 11: words.stream().filter(s -> !s.isEmpty())... -- readable, but negation via "!"
        // in front of a method reference isn't possible: you can't write !String::isEmpty.
        // Predicate.not(...) lets you negate a method reference directly.
        List<String> nonEmpty = words.stream()
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.toList());

        System.out.println("filter(Predicate.not(String::isEmpty)): " + nonEmpty);
    }

    private static void optionalIsEmpty() {
        Optional<String> present = Optional.of("value");
        Optional<String> absent = Optional.empty();

        // Before 11: !opt.isPresent() -- isEmpty() reads more naturally, especially with ifPresentOrElse.
        System.out.println("present.isEmpty(): " + present.isEmpty());
        System.out.println("absent.isEmpty(): " + absent.isEmpty());

        absent.ifPresentOrElse(
                v -> System.out.println("present: " + v),
                () -> System.out.println("ifPresentOrElse ran the empty branch"));
    }

    private static void collectionToArrayWithGenerator() {
        List<String> names = Arrays.asList("Ann", "Bob", "Cid");

        // Old API: Object[] or a manually pre-sized array, e.g. names.toArray(new String[0]).
        // JDK 11 adds toArray(IntFunction<T[]>), which reads more clearly and lets the collection
        // size the array itself instead of you guessing/allocating one.
        String[] array = names.toArray(String[]::new);
        System.out.println("names.toArray(String[]::new): " + Arrays.toString(array));
    }

    private static void patternAsMatchPredicate() {
        Pattern digitsOnly = Pattern.compile("\\d+");

        // Before 11: digitsOnly.matcher(s).matches() -- works, but you need a Matcher just to get a boolean.
        // asMatchPredicate() hands back a ready-to-use Predicate<String>.
        Predicate<String> isAllDigits = digitsOnly.asMatchPredicate();

        List<String> inputs = Arrays.asList("12345", "12a45", "999");
        for (String input : inputs) {
            System.out.println("isAllDigits.test(\"" + input + "\"): " + isAllDigits.test(input));
        }
    }
}
