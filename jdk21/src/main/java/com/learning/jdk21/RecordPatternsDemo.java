package com.learning.jdk21;

/**
 * JDK 21 (JEP 440): record patterns. Beyond the switch-based examples in
 * PatternMatchingSwitch, this focuses purely on the deconstruction mechanics
 * themselves -- var vs explicit types per component, partial deconstruction
 * with a mix, and how a record pattern interacts with generics.
 */
public class RecordPatternsDemo {

    record Point(int x, int y) {}

    record Line(Point start, Point end) {}

    record Named<T>(String name, T value) {}

    public static void main(String[] args) {
        explicitVsVarComponentTypes();
        deconstructingWithoutSwitch();
        deeplyNestedDeconstruction();
        genericRecordPattern();
    }

    private static void explicitVsVarComponentTypes() {
        Point p = new Point(3, 4);

        // Component patterns can spell out the exact type...
        if (p instanceof Point(int x, int y)) {
            System.out.println("explicit types Point(int x, int y): x=" + x + ", y=" + y);
        }

        // ...or let the compiler infer it with var, same as a normal local variable.
        if (p instanceof Point(var vx, var vy)) {
            System.out.println("var Point(var vx, var vy): vx=" + vx + ", vy=" + vy);
        }
    }

    private static void deconstructingWithoutSwitch() {
        Object obj = new Point(10, 20);

        // Record patterns work directly in a plain instanceof -- switch is not required.
        if (obj instanceof Point(int x, int y) && x > 0 && y > 0) {
            System.out.println("Point in the first quadrant: (" + x + ", " + y + ")");
        }
    }

    private static void deeplyNestedDeconstruction() {
        Line diagonal = new Line(new Point(0, 0), new Point(5, 5));

        // Nesting a record pattern inside another reaches straight through to the innermost fields --
        // no intermediate diagonal.start()/diagonal.end() calls needed.
        if (diagonal instanceof Line(Point(var x1, var y1), Point(var x2, var y2))) {
            double length = Math.hypot(x2 - x1, y2 - y1);
            System.out.println("Line(" + x1 + "," + y1 + " -> " + x2 + "," + y2 + ") length=" + length);
        }
    }

    private static void genericRecordPattern() {
        Named<Integer> namedAge = new Named<>("age", 42);
        Object obj = namedAge;

        // The outer type in the pattern must be a wildcard (Named<?>) -- Named<Integer> itself isn't
        // reifiable at runtime due to erasure, same restriction as plain "instanceof Named<Integer>".
        // The REAL check happens on the component pattern: "Integer value" performs an actual runtime
        // instanceof on that component, which is what makes this safe (and is why it matches here).
        if (obj instanceof Named<?>(String name, Integer value)) {
            System.out.println("Named<?>(String, Integer) deconstructed: " + name + " = " + value);
        }

        // Note: JDK 21 also previewed "_" as an unnamed pattern/variable (JEP 443) for exactly this
        // "I don't need this component" situation, e.g. Line(Point(var x1, var y1), Point _). It
        // needed --enable-preview in 21 and wasn't finalized until JDK 22 (JEP 456), so it's left out
        // of this module -- every demo here compiles and runs on a plain, non-preview JDK 21.
    }
}
