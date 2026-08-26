package com.learning.jdk21;

/**
 * JDK 21: pattern matching for switch (JEP 441) and record patterns (JEP 440),
 * both finalized after several rounds of preview. Reuses the sealed Shape hierarchy
 * idea but with switch expressions and nested record deconstruction instead of
 * chained instanceof.
 */
public class PatternMatchingSwitch {

    sealed interface Shape permits Circle, Rectangle {}

    record Point(double x, double y) {}

    record Circle(Point center, double radius) implements Shape {}

    record Rectangle(Point topLeft, double width, double height) implements Shape {}

    public double area(Shape shape) {
        return switch (shape) {
            case Circle(Point c, double r) -> Math.PI * r * r;
            case Rectangle(Point tl, double w, double h) -> w * h;
        };
    }

    public String describe(Object obj) {
        return switch (obj) {
            case Integer i when i < 0 -> "negative int";
            case Integer i -> "int: " + i;
            case String s -> "string of length " + s.length();
            case null -> "null value";
            default -> "something else: " + obj;
        };
    }

    // A deeper hierarchy to show nested record-pattern deconstruction across more than one level.
    record Colored<S>(S shape, String colorName) {}

    public static void main(String[] args) {
        PatternMatchingSwitch demo = new PatternMatchingSwitch();

        recordPatternDeconstruction(demo);
        guardedPatternsWithWhen(demo);
        nullHandlingInSwitch(demo);
        nestedRecordPatterns();
        recordPatternInInstanceof();
        exhaustiveSealedSwitchNoDefault();
    }

    private static void recordPatternDeconstruction(PatternMatchingSwitch demo) {
        Shape circle = new Circle(new Point(0, 0), 2);
        Shape rectangle = new Rectangle(new Point(1, 1), 3, 4);

        // "case Circle(Point c, double r) ->" deconstructs the record's components directly in the
        // case label -- no manual circle.center()/circle.radius() calls needed inside the arm.
        System.out.println("area(Circle): " + demo.area(circle));
        System.out.println("area(Rectangle): " + demo.area(rectangle));
    }

    private static void guardedPatternsWithWhen(PatternMatchingSwitch demo) {
        System.out.println("describe(-5): " + demo.describe(-5));
        System.out.println("describe(5): " + demo.describe(5));
        System.out.println("describe(\"hi\"): " + demo.describe("hi"));
        System.out.println("describe(3.14): " + demo.describe(3.14));
    }

    private static void nullHandlingInSwitch(PatternMatchingSwitch demo) {
        // Before 21, switching on a null selector always threw NullPointerException immediately.
        // "case null ->" lets the switch itself handle null as an ordinary case, right alongside
        // type patterns.
        System.out.println("describe(null): " + demo.describe(null));
    }

    private static void nestedRecordPatterns() {
        Colored<Circle> redCircle = new Colored<>(new Circle(new Point(2, 3), 5), "red");

        // Patterns nest arbitrarily deep: this reaches through Colored -> Circle -> Point in one
        // case label, binding x/y/radius/colorName all at once.
        String description = switch (redCircle) {
            case Colored<?>(Circle(Point(var x, var y), var radius), var colorName) ->
                    colorName + " circle at (" + x + "," + y + ") with radius " + radius;
            default -> "not a colored circle";
        };
        System.out.println("nested record pattern (Colored<Circle(Point(x,y),r)>): " + description);
    }

    private static void recordPatternInInstanceof() {
        // Record patterns work with instanceof too, not just switch -- same deconstruction syntax.
        Object obj = new Circle(new Point(7, 8), 1.5);
        if (obj instanceof Circle(Point(var x, var y), var radius)) {
            System.out.println("instanceof record pattern: center=(" + x + "," + y + "), radius=" + radius);
        }
    }

    private static void exhaustiveSealedSwitchNoDefault() {
        // Shape is sealed with exactly two permitted subtypes (Circle, Rectangle). Because the
        // switch covers both, the compiler proves exhaustiveness on its own -- no default needed,
        // unlike Object-typed switches (describe() above) which must always have one.
        for (Shape shape : java.util.List.of(new Circle(new Point(0, 0), 1), new Rectangle(new Point(0, 0), 2, 3))) {
            String kind = switch (shape) {
                case Circle c -> "circle";
                case Rectangle r -> "rectangle";
            };
            System.out.println("exhaustive sealed switch, no default: " + kind);
        }
    }
}
