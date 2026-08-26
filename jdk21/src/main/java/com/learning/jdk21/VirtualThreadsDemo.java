package com.learning.jdk21;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * JDK 21 (JEP 444): virtual threads. Lightweight threads scheduled by the JVM
 * onto a small pool of OS ("carrier") threads, instead of each Thread owning
 * an OS thread 1:1. Virtual threads are cheap enough to create *millions* of,
 * which makes "one thread per task/request" viable again for blocking,
 * I/O-heavy workloads -- no reactive/callback rewrite needed.
 */
public class VirtualThreadsDemo {

    public List<String> fetchAllConcurrently(List<Callable<String>> tasks) throws InterruptedException, ExecutionException {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = executor.invokeAll(tasks);
            return futures.stream().map(f -> {
                try {
                    return f.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
        }
    }

    public static void main(String[] args) throws Exception {
        creatingAVirtualThreadDirectly();
        virtualThreadIsVirtualFlag();
        executorServiceOfVirtualThreads();
        manyVirtualThreadsBlockingConcurrently();
    }

    private static void creatingAVirtualThreadDirectly() throws InterruptedException {
        // Thread.ofVirtual() is the builder-style API for creating a single virtual thread,
        // mirroring Thread.ofPlatform() for traditional OS-backed threads.
        Thread vThread = Thread.ofVirtual()
                .name("demo-virtual-thread")
                .start(() -> System.out.println("  running on: " + Thread.currentThread()));
        vThread.join();
    }

    private static void virtualThreadIsVirtualFlag() throws InterruptedException {
        Thread platform = new Thread(() -> {});
        Thread virtual = Thread.ofVirtual().unstarted(() -> {});

        // Thread.isVirtual() lets code (e.g. a framework) detect which kind of thread it's on.
        System.out.println("new Thread(...).isVirtual(): " + platform.isVirtual());
        System.out.println("Thread.ofVirtual().unstarted(...).isVirtual(): " + virtual.isVirtual());
    }

    private static void executorServiceOfVirtualThreads() throws InterruptedException, ExecutionException {
        VirtualThreadsDemo demo = new VirtualThreadsDemo();
        List<Callable<String>> tasks = List.of(
                () -> simulateBlockingCall("task-A", 50),
                () -> simulateBlockingCall("task-B", 30),
                () -> simulateBlockingCall("task-C", 70));

        List<String> results = demo.fetchAllConcurrently(tasks);
        System.out.println("newVirtualThreadPerTaskExecutor() results: " + results);
    }

    private static String simulateBlockingCall(String name, long millis) throws InterruptedException {
        Thread.sleep(millis); // a *real* blocking call -- exactly what virtual threads are built for
        return name + " done";
    }

    private static void manyVirtualThreadsBlockingConcurrently() throws InterruptedException {
        // The headline claim of JEP 444: spinning up huge numbers of virtual threads that each just
        // block is cheap, because blocking a virtual thread only "parks" it -- the underlying carrier
        // OS thread is freed up to run other virtual threads in the meantime, instead of sitting idle.
        int taskCount = 10_000;
        AtomicInteger completed = new AtomicInteger();
        Instant start = Instant.now();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, taskCount).forEach(i -> executor.submit(() -> {
                try {
                    Thread.sleep(10); // each task blocks briefly -- would be very expensive with 10,000 platform threads
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                completed.incrementAndGet();
            }));
        } // try-with-resources on an ExecutorService (JDK 19+) waits for shutdown before continuing

        Duration elapsed = Duration.between(start, Instant.now());
        System.out.println(taskCount + " virtual threads, each blocking ~10ms, all completed: "
                + completed.get() + " in " + elapsed.toMillis() + "ms (heavily overlapped, not "
                + (taskCount * 10) + "ms serial)");
    }
}
