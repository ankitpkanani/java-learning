package com.learning.jdk8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * JDK 8: method references are shorthand for a lambda that does nothing but
 * call an existing method. There are four kinds, distinguished by what comes
 * before the "::".
 */
public class MethodReferenceDemo {

    public static void main(String[] args) {
        staticMethodReference();
        boundInstanceMethodReference();
        unboundInstanceMethodReference();
        constructorReference();
    }

    // 1) Reference to a static method: TypeName::staticMethod
    private static void staticMethodReference() {
        Function<String, Integer> parse = Integer::parseInt; // same as: s -> Integer.parseInt(s)
        System.out.println("Integer::parseInt applied to \"42\": " + parse.apply("42"));
    }

    // 2) Reference to an instance method of a *particular* object: instance::method
    private static void boundInstanceMethodReference() {
        String greeting = "Hello, method references!";
        Supplier<Integer> lengthOfGreeting = greeting::length; // same as: () -> greeting.length()
        System.out.println("greeting::length (bound to a specific String): " + lengthOfGreeting.get());

        List<String> collected = new ArrayList<>();
        java.util.function.Consumer<String> addToList = collected::add; // bound to this specific list
        addToList.accept("first");
        addToList.accept("second");
        System.out.println("collected via a bound instance method reference: " + collected);
    }

    // 3) Reference to an instance method of an *arbitrary* object of a particular type:
    //    TypeName::instanceMethod -- the first lambda parameter becomes the receiver.
    private static void unboundInstanceMethodReference() {
        Function<String, String> upper = String::toUpperCase; // same as: s -> s.toUpperCase()
        System.out.println("String::toUpperCase applied to \"quiet\": " + upper.apply("quiet"));

        BiFunction<String, String, Boolean> startsWith = String::startsWith; // (s, prefix) -> s.startsWith(prefix)
        System.out.println("String::startsWith(\"lambda\", \"lam\"): " + startsWith.apply("lambda", "lam"));

        List<String> names = Arrays.asList("Charlie", "alice", "Bob");
        names.sort(String::compareToIgnoreCase); // Comparator<String> from an unbound instance method
        System.out.println("sorted with String::compareToIgnoreCase: " + names);
    }

    // 4) Reference to a constructor: TypeName::new
    private static void constructorReference() {
        Supplier<ArrayList<String>> newList = ArrayList::new; // same as: () -> new ArrayList<>()
        List<String> list = newList.get();
        list.add("built via a constructor reference");
        System.out.println(list);

        Function<String, StringBuilder> newBuilder = StringBuilder::new; // picks the (String) constructor
        System.out.println("StringBuilder::new applied to \"abc\": " + newBuilder.apply("abc").reverse());
    }
}
