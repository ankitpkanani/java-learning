package com.learning.jdk8;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * JDK 8: the Streams API is Java's implementation of the map-reduce
 * programming model -- "map" transforms each element independently, "reduce"
 * folds all the (possibly transformed) elements into a single result. Streams
 * can run this in parallel across CPU cores, which is exactly why the reduce
 * step needs an extra piece beyond a simple 2-argument fold.
 */
public class MapReduceDemo {

    static final class Order {
        private final String product;
        private final int quantity;
        private final double unitPrice;

        Order(String product, int quantity, double unitPrice) {
            this.product = product;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        String product() {
            return product;
        }

        double lineTotal() {
            return quantity * unitPrice;
        }
    }

    public static void main(String[] args) {
        basicMapThenReduce();
        threeArgReduceExplained();
        parallelMapReduceProvesTheCombinerRuns();
        classicWordCountMapReduce();
    }

    private static void basicMapThenReduce() {
        List<Order> orders = Arrays.asList(
                new Order("Widget", 3, 9.99),
                new Order("Gadget", 1, 49.50),
                new Order("Widget", 2, 9.99));

        // map: Order -> its line total (the "map" step, one output per input, in isolation)
        // reduce: fold all the line totals into a single sum (the "reduce" step)
        double grandTotal = orders.stream()
                .map(Order::lineTotal)
                .reduce(0.0, Double::sum);

        System.out.println("map(Order::lineTotal).reduce(0.0, Double::sum): " + grandTotal);
    }

    private static void threeArgReduceExplained() {
        List<String> words = Arrays.asList("map", "reduce", "is", "core", "to", "streams");

        // 2-arg reduce: BinaryOperator<T> accumulator, input type == result type.
        int totalLengthSimple = words.stream()
                .map(String::length)     // map: String -> Integer
                .reduce(0, Integer::sum); // reduce: Integer,Integer -> Integer (types already match)

        // 3-arg reduce: reduce(identity, accumulator, combiner) lets the *reduce* step itself do the
        // mapping (T -> U) while accumulating, and separately says how to merge two partial U results.
        // The combiner is what makes this safe to parallelize: on a parallel stream, the source is
        // split into chunks, each chunk is folded independently with the accumulator, and then the
        // combiner merges each chunk's partial result together. On a sequential stream the combiner
        // is simply never called.
        int totalLengthViaThreeArgReduce = words.stream()
                .reduce(0,                                      // identity: the starting U
                        (partialSum, word) -> partialSum + word.length(), // accumulator: (U, T) -> U
                        Integer::sum);                           // combiner: (U, U) -> U, merges partial results

        System.out.println("2-arg reduce after a separate map(): " + totalLengthSimple);
        System.out.println("3-arg reduce doing the mapping inline: " + totalLengthViaThreeArgReduce);
    }

    private static void parallelMapReduceProvesTheCombinerRuns() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        AtomicInteger combinerInvocations = new AtomicInteger();

        int sumOfSquares = numbers.parallelStream()
                .reduce(0,
                        (partialSum, n) -> partialSum + n * n, // accumulator does the "map" (square) + fold
                        (left, right) -> {                      // combiner: merges two threads' partial sums
                            combinerInvocations.incrementAndGet();
                            return left + right;
                        });

        System.out.println("parallelStream() 3-arg reduce sum of squares: " + sumOfSquares);
        System.out.println("combiner was invoked " + combinerInvocations.get()
                + " time(s) to merge partial results from different threads");
    }

    private static void classicWordCountMapReduce() {
        List<String> lines = Arrays.asList(
                "the quick brown fox",
                "the lazy dog sleeps",
                "the fox jumps over the dog");

        // The textbook MapReduce example: map each line to its words, then reduce by counting
        // occurrences per key. Collectors.groupingBy + counting() is the Streams equivalent of
        // "map each word to (word, 1), then reduce by summing counts per key".
        Map<String, Long> wordCounts = lines.stream()
                .flatMap(line -> Arrays.stream(line.split("\\s+"))) // map: line -> stream of words
                .collect(Collectors.groupingBy(word -> word, Collectors.counting())); // reduce: count per key

        System.out.println("classic word-count map-reduce: " + wordCounts);
    }
}
