package com.learning.jdk25;

import java.util.List;
import java.util.Optional;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

/**
 * JDK 24 (JEP 485, carried forward into 25): Stream Gatherers. Streams gained
 * a fixed, closed set of intermediate operations (map/filter/flatMap/...) in
 * JDK 8, with no way to add your own without collect()-ing to something else
 * first. Stream.gather(Gatherer) opens that up: a Gatherer is a fully custom,
 * composable intermediate operation, and java.util.stream.Gatherers ships a
 * handful of ready-made ones for very common needs.
 */
public class StreamGatherersDemo {

    public static void main(String[] args) {
        builtInWindowFixed();
        builtInWindowSliding();
        builtInFold();
        builtInScan();
        customGatherer();
        composingGatherersWithAndThen();
    }

    private static void builtInWindowFixed() {
        // Fixed, non-overlapping windows -- the last one may be short if the count doesn't divide evenly.
        List<List<Integer>> windows = Stream.of(1, 2, 3, 4, 5, 6, 7, 8)
                .gather(Gatherers.windowFixed(3))
                .toList();
        System.out.println("Gatherers.windowFixed(3): " + windows);
    }

    private static void builtInWindowSliding() {
        // Overlapping windows: each subsequent window drops the oldest element and adds the next one.
        List<List<Integer>> windows = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.windowSliding(2))
                .toList();
        System.out.println("Gatherers.windowSliding(2): " + windows);
    }

    private static void builtInFold() {
        // fold(): an order-dependent reduction to a SINGLE final result -- like reduce(), but for
        // cases where left-to-right order matters and no combiner function could make sense
        // (e.g. building up a String, where "a" + "b" != "b" + "a").
        Optional<String> result = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.fold(() -> "", (acc, n) -> acc + n))
                .findFirst();
        System.out.println("Gatherers.fold(...) concatenating digits: " + result.orElse(""));
    }

    private static void builtInScan() {
        // scan(): like fold(), but emits every INTERMEDIATE accumulated value, not just the final one --
        // a running/prefix computation, one output element per input element.
        List<Integer> runningTotals = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.scan(() -> 0, Integer::sum))
                .toList();
        System.out.println("Gatherers.scan(...) running totals: " + runningTotals);
    }

    private static void customGatherer() {
        // A from-scratch custom Gatherer: emit only elements strictly greater than every element
        // seen so far (a "running maximum" filter) -- there's no built-in stream operation for this.
        Gatherer<Integer, ?, Integer> newRunningMaximums = runningMaximums();

        List<Integer> peaks = Stream.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 9)
                .gather(newRunningMaximums)
                .toList();
        System.out.println("custom Gatherer (running maximums) over 3,1,4,1,5,9,2,6,5,3,9: " + peaks);
    }

    private static Gatherer<Integer, ?, Integer> runningMaximums() {
        class State {
            int max = Integer.MIN_VALUE;
        }

        // Gatherer.ofSequential(initializer, integrator): initializer creates the per-stream mutable
        // state; the integrator runs once per element, deciding whether/what to push downstream, and
        // returns whether the gatherer wants to keep receiving elements (true) or can stop early (false).
        return Gatherer.ofSequential(
                State::new,
                Gatherer.Integrator.<State, Integer, Integer>ofGreedy((state, element, downstream) -> {
                    if (element > state.max) {
                        state.max = element;
                        return downstream.push(element);
                    }
                    return true; // not a new maximum -- consume it, but push nothing downstream
                }));
    }

    private static void composingGatherersWithAndThen() {
        // Gatherers compose with andThen(...), the same way Function composes -- build small, reusable
        // gatherers and chain them, instead of writing one large bespoke gatherer per pipeline.
        Gatherer<Integer, ?, Integer> increment = map(n -> n + 1);
        Gatherer<Integer, ?, String> toLabeledString = map(n -> "#" + n);
        Gatherer<Integer, ?, String> incrementThenLabel = increment.andThen(toLabeledString);

        List<String> result = Stream.of(1, 2, 3)
                .gather(incrementThenLabel)
                .toList();
        System.out.println("increment.andThen(toLabeledString) over 1,2,3: " + result);
    }

    private static <T, R> Gatherer<T, ?, R> map(java.util.function.Function<? super T, ? extends R> mapper) {
        return Gatherer.of((unused, element, downstream) -> downstream.push(mapper.apply(element)));
    }
}
