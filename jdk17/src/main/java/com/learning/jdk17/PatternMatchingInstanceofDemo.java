package com.learning.jdk17;

import java.util.List;

/**
 * JDK 16 (JEP 394): pattern matching for instanceof. The old idiom was
 * "instanceof check, then an explicit cast" as two separate steps; now the
 * cast is folded into the instanceof itself, and the compiler tracks exactly
 * where the resulting "pattern variable" is definitely safe to use.
 */
public class PatternMatchingInstanceofDemo {

    public static void main(String[] args) {
        beforeAndAfter();
        flowScopingWithAnd();
        flowScopingWithNegationAndReturn();
        combinedWithSealedHierarchy();
    }

    private static void beforeAndAfter() {
        Object obj = "hello pattern matching";

        // The old, pre-16 way: two steps, and a cast that can theoretically go stale if the
        // code above it changes.
        if (obj instanceof String) {
            String s = (String) obj;
            System.out.println("old-style cast: length=" + s.length());
        }

        // JDK 16+: the pattern variable "s" is bound directly by instanceof.
        if (obj instanceof String s) {
            System.out.println("pattern-matched instanceof: length=" + s.length());
        }
    }

    private static void flowScopingWithAnd() {
        Object obj = "  padded  ";

        // The pattern variable's scope extends into the REST of a "&&" expression, because if the
        // instanceof check on the left is false, Java short-circuits and never evaluates the right
        // side -- so "s" is guaranteed bound wherever it is actually read.
        if (obj instanceof String s && !s.isBlank() && s.trim().length() > 3) {
            System.out.println("chained && conditions reusing 's': trimmed length = " + s.trim().length());
        }
    }

    private static void flowScopingWithNegationAndReturn() {
        System.out.println(describe(42));
        System.out.println(describe("not a number"));
    }

    private static String describe(Object obj) {
        // The pattern variable's scope can extend PAST the if-block too, when the compiler can prove
        // the only way to reach the code after it is with the pattern having matched -- here, the
        // negated branch returns early, so by the time we reach the final line, "n" must be bound.
        if (!(obj instanceof Integer n)) {
            return "not an Integer: " + obj;
        }
        return "Integer value squared: " + (n * n);
    }

    private static void combinedWithSealedHierarchy() {
        // Shape.area() (in Shape.java) is itself built entirely out of pattern-matching instanceof
        // over a sealed interface -- this just exercises it here with a mixed list.
        List<Shape> shapes = List.of(new Shape.Circle(1), new Shape.Rectangle(2, 3), new Shape.Triangle(4, 5));
        for (Shape shape : shapes) {
            System.out.printf("area(%s) = %.2f%n", shape, Shape.area(shape));
        }
    }
}
