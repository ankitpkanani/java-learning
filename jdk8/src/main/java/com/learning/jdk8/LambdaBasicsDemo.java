package com.learning.jdk8;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * JDK 8: lambda expressions (JSR 335) and the core functional interfaces in
 * java.util.function that give them a target type.
 *
 * A lambda has no type of its own -- it is only valid where the compiler can
 * infer a "functional interface" (an interface with exactly one abstract
 * method) from context. That single abstract method's signature is what
 * determines how many arguments the lambda takes and what it must return.
 */
public class LambdaBasicsDemo {

    // A custom functional interface. @FunctionalInterface is optional but makes the
    // compiler enforce "exactly one abstract method" -- catches accidental extra methods.
    @FunctionalInterface
    interface Greeter {
        String greet(String name);
    }

    public static void main(String[] args) {
        noArgLambda();
        singleArgLambdaWithAndWithoutParens();
        multiArgLambdaWithBlockBody();
        variableCaptureMustBeEffectivelyFinal();
        customFunctionalInterface();
        coreFunctionalInterfaces();
    }

    private static void noArgLambda() {
        Supplier<String> greeting = () -> "hello";
        System.out.println("Supplier<String> () -> \"hello\": " + greeting.get());
    }

    private static void singleArgLambdaWithAndWithoutParens() {
        // Single-parameter lambdas can drop the parentheses when the type is inferred.
        Function<String, Integer> length1 = s -> s.length();
        Function<String, Integer> length2 = (String s) -> s.length(); // explicit type also allowed
        System.out.println("length(\"lambda\") via s -> s.length(): " + length1.apply("lambda"));
        System.out.println("length(\"lambda\") via (String s) -> ...: " + length2.apply("lambda"));
    }

    private static void multiArgLambdaWithBlockBody() {
        BiFunction<Integer, Integer, Integer> maxOf = (a, b) -> {
            // A block body needs an explicit return, unlike a single-expression lambda.
            if (a >= b) {
                return a;
            }
            return b;
        };
        System.out.println("maxOf(3, 7) via block-bodied lambda: " + maxOf.apply(3, 7));
    }

    private static void variableCaptureMustBeEffectivelyFinal() {
        int threshold = 10; // never reassigned after this point -> "effectively final"
        Predicate<Integer> aboveThreshold = n -> n > threshold;
        System.out.println("aboveThreshold.test(15) capturing local 'threshold': " + aboveThreshold.test(15));
        // threshold = 20; // would NOT compile: a lambda can only capture effectively-final locals
    }

    private static void customFunctionalInterface() {
        Greeter formal = name -> "Good day, " + name + ".";
        Greeter casual = name -> "Hey " + name + "!";
        System.out.println("formal.greet(\"Ada\"): " + formal.greet("Ada"));
        System.out.println("casual.greet(\"Ada\"): " + casual.greet("Ada"));
    }

    private static void coreFunctionalInterfaces() {
        Predicate<String> isBlank = String::isEmpty;
        Function<String, Integer> toLength = String::length;
        Consumer<String> printIt = s -> System.out.println("  Consumer printed: " + s);
        Supplier<String> defaultName = () -> "anonymous";
        UnaryOperator<Integer> square = n -> n * n;
        BinaryOperator<Integer> sum = Integer::sum;

        System.out.println("Predicate<String> isBlank.test(\"\"): " + isBlank.test(""));
        System.out.println("Function<String,Integer> toLength.apply(\"abcd\"): " + toLength.apply("abcd"));
        printIt.accept("value from a Consumer");
        System.out.println("Supplier<String> defaultName.get(): " + defaultName.get());
        System.out.println("UnaryOperator<Integer> square.apply(6): " + square.apply(6));
        System.out.println("BinaryOperator<Integer> sum.apply(4, 5): " + sum.apply(4, 5));

        // Predicate composition: and/or/negate build new predicates without writing new lambdas.
        Predicate<String> isShort = s -> s.length() <= 3;
        Predicate<String> isShortAndBlank = isShort.and(isBlank);
        Predicate<String> isShortOrBlank = isShort.or(isBlank);
        Predicate<String> isNotShort = isShort.negate();
        System.out.println("isShort.and(isBlank).test(\"\"): " + isShortAndBlank.test(""));
        System.out.println("isShort.or(isBlank).test(\"abcdef\"): " + isShortOrBlank.test("abcdef"));
        System.out.println("isShort.negate().test(\"ab\"): " + isNotShort.test("ab"));

        // Function composition: andThen runs after, compose runs before.
        Function<Integer, Integer> addOne = n -> n + 1;
        Function<Integer, Integer> triple = n -> n * 3;
        System.out.println("addOne.andThen(triple).apply(4) = (4+1)*3: " + addOne.andThen(triple).apply(4));
        System.out.println("addOne.compose(triple).apply(4) = (4*3)+1: " + addOne.compose(triple).apply(4));
    }
}
