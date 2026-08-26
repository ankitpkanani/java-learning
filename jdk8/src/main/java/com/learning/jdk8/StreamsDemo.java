package com.learning.jdk8;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * JDK 8: lambda expressions (JSR 335), the Streams API, and method references
 * for declarative collection processing instead of manual loops.
 */
public class StreamsDemo {

    public List<String> upperCasedEvenLengthNames(List<String> names) {
        return names.stream()
                .filter(name -> name.length() % 2 == 0)
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.toList());
    }

    public int sumOfLengths(List<String> names) {
        return names.stream()
                .mapToInt(String::length)
                .sum();
    }

    public static void main(String[] args) {
        intermediateOperations();
        terminalOperations();
        flatMapDemo();
        primitiveStreams();
        laziness();
        parallelStream();
    }

    private static void intermediateOperations() {
        List<String> words = Arrays.asList("banana", "apple", "kiwi", "apple", "fig", "cherry");

        List<String> pipeline = words.stream()
                .distinct()                       // remove duplicate "apple"
                .filter(w -> w.length() > 3)       // drop short words
                .map(String::toUpperCase)          // transform
                .sorted()                          // natural order
                .skip(1)                           // drop the first result
                .limit(3)                          // keep at most 3
                .collect(Collectors.toList());

        System.out.println("distinct->filter->map->sorted->skip->limit: " + pipeline);
    }

    private static void terminalOperations() {
        List<Integer> numbers = Arrays.asList(4, 9, 15, 20, 2, 33);

        long countAboveTen = numbers.stream().filter(n -> n > 10).count();
        boolean anyNegative = numbers.stream().anyMatch(n -> n < 0);
        boolean allPositive = numbers.stream().allMatch(n -> n > 0);
        boolean noneOverHundred = numbers.stream().noneMatch(n -> n > 100);
        int sumViaReduce = numbers.stream().reduce(0, Integer::sum);
        java.util.Optional<Integer> max = numbers.stream().reduce(Integer::max);

        System.out.println("count(n > 10): " + countAboveTen);
        System.out.println("anyMatch(n < 0): " + anyNegative);
        System.out.println("allMatch(n > 0): " + allPositive);
        System.out.println("noneMatch(n > 100): " + noneOverHundred);
        System.out.println("reduce(0, Integer::sum): " + sumViaReduce);
        System.out.println("reduce(Integer::max): " + max.orElse(null));
    }

    private static void flatMapDemo() {
        List<List<Integer>> nested = Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5),
                Arrays.asList(6));

        // flatMap turns a Stream<List<Integer>> into a single flat Stream<Integer>.
        List<Integer> flattened = nested.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        System.out.println("flatMap flattens List<List<Integer>>: " + flattened);
    }

    private static void primitiveStreams() {
        // IntStream/LongStream/DoubleStream avoid boxing every element into an Integer/Long/Double.
        int sumOfSquares = IntStream.rangeClosed(1, 5)
                .map(n -> n * n)
                .sum();
        OptionalDouble average = IntStream.rangeClosed(1, 5).average();

        System.out.println("IntStream.rangeClosed(1,5).map(square).sum(): " + sumOfSquares);
        System.out.println("IntStream.rangeClosed(1,5).average(): " + average.orElse(0));
    }

    private static void laziness() {
        // Nothing prints here: intermediate operations are lazy, they only describe a pipeline.
        Stream<String> lazyPipeline = Stream.of("a", "b", "c")
                .peek(s -> System.out.println("  peek saw: " + s))
                .map(String::toUpperCase);

        System.out.println("Pipeline built, but no peek() output yet (stream is lazy).");
        System.out.println("Now consuming with collect():");
        List<String> result = lazyPipeline.collect(Collectors.toList()); // this triggers everything above
        System.out.println("Result: " + result);
    }

    private static void parallelStream() {
        long start = System.nanoTime();
        long total = IntStream.rangeClosed(1, 5_000_000)
                .parallel()
                .asLongStream()
                .sum();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("parallel() sum of 1..5,000,000 = " + total + " (" + elapsedMs + " ms)");
    }
}
