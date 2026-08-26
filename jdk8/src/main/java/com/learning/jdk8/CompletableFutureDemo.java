package com.learning.jdk8;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * JDK 8: CompletableFuture composes asynchronous steps -- run on a background
 * thread, transform the result, combine two futures, or recover from a
 * failure -- without hand-writing callback/thread-coordination code.
 */
public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        supplyAndTransform();
        combineTwoFutures();
        recoverFromFailure();
        waitForAllOf();
    }

    private static void supplyAndTransform() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> slow(21)) // runs on the common ForkJoinPool
                .thenApply(n -> n * 2)       // transform the result once it's ready
                .thenApply(n -> "answer = " + n);

        // .get() blocks the calling thread until the async chain completes.
        System.out.println("supplyAsync -> thenApply -> thenApply: " + future.get());
    }

    private static void combineTwoFutures() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> price = CompletableFuture.supplyAsync(() -> slow(50));
        CompletableFuture<Double> taxRate = CompletableFuture.supplyAsync(() -> 0.18);

        CompletableFuture<Double> total = price.thenCombine(taxRate, (p, rate) -> p * (1 + rate));
        System.out.println("thenCombine(price, taxRate): " + total.get());
    }

    private static void recoverFromFailure() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> risky = CompletableFuture
                .<Integer>supplyAsync(() -> {
                    throw new IllegalStateException("simulated failure");
                })
                .exceptionally(ex -> {
                    System.out.println("  exceptionally() caught: " + ex.getCause().getMessage());
                    return -1; // fallback value so the chain still completes
                });
        System.out.println("exceptionally() fallback result: " + risky.get());
    }

    private static void waitForAllOf() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> slow(1));
        CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> slow(2));
        CompletableFuture<Integer> c = CompletableFuture.supplyAsync(() -> slow(3));

        // allOf itself completes with Void; read each source future once you know they're all done.
        CompletableFuture<List<Integer>> all = CompletableFuture.allOf(a, b, c)
                .thenApply(ignored -> java.util.Arrays.asList(a.join(), b.join(), c.join()));

        System.out.println("CompletableFuture.allOf(a, b, c): " + all.get());
    }

    private static int slow(int value) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return value;
    }
}
