package com.learning.jdk11;

import java.util.function.BiFunction;

/**
 * JDK 11 (JEP 323): local-variable syntax for lambda parameters. "var" was
 * introduced in JDK 10 for local variables only; JDK 11 extends it to lambda
 * parameter lists purely for *consistency* and so lambda parameters can carry
 * annotations/modifiers the same way a normal local variable declaration can.
 * It has no effect on type inference -- the compiler still infers the exact
 * same type it would have without "var".
 */
public class VarInLambdaParamsDemo {

    // A trivial marker annotation to show *why* var-in-lambda-params exists: without "var" there
    // is no syntax slot to attach an annotation (or a modifier like "final") to a lambda parameter.
    @interface NonEmpty {
    }

    public static void main(String[] args) {
        implicitVsVarParams();
        allOrNothingRule();
        annotatingALambdaParameter();
    }

    private static void implicitVsVarParams() {
        // Implicitly-typed params (most common style): no type, no var.
        BiFunction<Integer, Integer, Integer> add1 = (a, b) -> a + b;

        // Explicitly-typed params: spells out the type.
        BiFunction<Integer, Integer, Integer> add2 = (Integer a, Integer b) -> a + b;

        // var params (JDK 11): same inferred type as the implicit form, spelled out via "var".
        BiFunction<Integer, Integer, Integer> add3 = (var a, var b) -> a + b;

        System.out.println("(a, b) -> a + b            applied to (3, 4): " + add1.apply(3, 4));
        System.out.println("(Integer a, Integer b) -> ... applied to (3, 4): " + add2.apply(3, 4));
        System.out.println("(var a, var b) -> a + b     applied to (3, 4): " + add3.apply(3, 4));
    }

    private static void allOrNothingRule() {
        // The parameter list must be consistent: either ALL params use var, or NONE do. You cannot
        // mix "var" with implicit or explicit types in the same lambda -- that's a compile error:
        //
        //   BiFunction<Integer, Integer, Integer> invalid = (var a, b) -> a + b; // won't compile
        //
        System.out.println("Lambda params must be all-var or all-not-var (mixing is a compile error).");
    }

    private static void annotatingALambdaParameter() {
        // This is the actual motivating use case for JEP 323: a lambda parameter can now carry an
        // annotation, which was impossible with the implicitly-typed "(a, b) -> ..." form.
        BiFunction<String, String, String> join = (@NonEmpty var left, @NonEmpty var right) -> left + right;
        System.out.println("(@NonEmpty var left, @NonEmpty var right) -> left + right: "
                + join.apply("foo", "bar"));

        // final is a modifier, same story -- only reachable once the parameter has an explicit
        // type-position to attach it to.
        BiFunction<Integer, Integer, Integer> multiply = (final var a, final var b) -> a * b;
        System.out.println("(final var a, final var b) -> a * b applied to (6, 7): " + multiply.apply(6, 7));
    }
}
