package com.learning.jdk25;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JDK 25 (JEP 506): Scoped Values, finalized after preview rounds since 21.
 * A ScopedValue is bound for the dynamic extent of one call -- itself and
 * everything it calls (including child threads it starts) -- and is
 * automatically un-bound when that call returns. Unlike ThreadLocal, there is
 * no explicit set()/remove() to forget, no mutability once bound, and child
 * threads inherit the binding safely instead of needing InheritableThreadLocal.
 */
public class ScopedValuesDemo {

    private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
    private static final ScopedValue<String> USER = ScopedValue.newInstance();

    public String currentRequestId() {
        return REQUEST_ID.isBound() ? REQUEST_ID.get() : "none";
    }

    public <T> T runWithRequestId(String requestId, Callable<T> action) throws Exception {
        return ScopedValue.where(REQUEST_ID, requestId).call(action::call);
    }

    public static void main(String[] args) throws Exception {
        basicBindAndUnbind();
        multipleValuesInOneScope();
        nestedRebinding();
        inheritedByChildThreads();
        orElseDefault();
    }

    private static void basicBindAndUnbind() throws Exception {
        System.out.println("REQUEST_ID.isBound() before any binding: " + REQUEST_ID.isBound());

        // ScopedValue.where(...).run(...) binds REQUEST_ID for the duration of the lambda only --
        // it's automatically unbound the instant run() returns, no cleanup code needed.
        ScopedValue.where(REQUEST_ID, "req-100").run(() ->
                System.out.println("inside run(): REQUEST_ID = " + REQUEST_ID.get()));

        System.out.println("REQUEST_ID.isBound() after run() returns: " + REQUEST_ID.isBound());
    }

    private static void multipleValuesInOneScope() throws Exception {
        // where(...).where(...) chains multiple bindings into a single Carrier before calling/running.
        ScopedValue.where(REQUEST_ID, "req-200")
                .where(USER, "ada")
                .run(() -> System.out.println("multiple bindings: REQUEST_ID=" + REQUEST_ID.get()
                        + ", USER=" + USER.get()));
    }

    private static void nestedRebinding() throws Exception {
        // A nested where(...).run(...) can rebind REQUEST_ID for just its own inner scope; the outer
        // binding is restored automatically once the inner run() returns -- no manual save/restore.
        ScopedValue.where(REQUEST_ID, "outer-req").run(() -> {
            System.out.println("outer scope: REQUEST_ID = " + REQUEST_ID.get());
            try {
                ScopedValue.where(REQUEST_ID, "inner-req").run(() ->
                        System.out.println("  inner (rebound) scope: REQUEST_ID = " + REQUEST_ID.get()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("back in outer scope, automatically restored: REQUEST_ID = " + REQUEST_ID.get());
        });
    }

    private static void inheritedByChildThreads() throws Exception {
        // A binding is visible to virtual threads started from within the bound scope -- structured,
        // predictable inheritance, unlike InheritableThreadLocal (which copies eagerly at thread
        // creation time and keeps its own independent, mutable copy from then on).
        ScopedValue.where(REQUEST_ID, "req-300").run(() -> {
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                executor.submit(() ->
                        System.out.println("virtual thread sees inherited REQUEST_ID: " + REQUEST_ID.get()));
            }
        });
    }

    private static void orElseDefault() {
        // get() throws NoSuchElementException if unbound; orElse(...) gives a safe fallback instead.
        System.out.println("REQUEST_ID.orElse(\"unbound\") with nothing bound: " + REQUEST_ID.orElse("unbound"));
    }
}
