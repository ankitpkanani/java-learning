package com.learning.jdk17;

/**
 * JDK 17: sealed classes/interfaces (JEP 409) restrict which types may implement them.
 * Combined with records (JEP 395, finalized in 16) for compact immutable data carriers,
 * and pattern matching for instanceof (JEP 394, finalized in 16).
 */
public sealed interface Shape {

    record Circle(double radius) implements Shape {}

    record Rectangle(double width, double height) implements Shape {}

    record Triangle(double base, double height) implements Shape {}

    static double area(Shape shape) {
        if (shape instanceof Circle c) {
            return Math.PI * c.radius() * c.radius();
        } else if (shape instanceof Rectangle r) {
            return r.width() * r.height();
        } else if (shape instanceof Triangle t) {
            return 0.5 * t.base() * t.height();
        }
        throw new IllegalStateException("Unreachable: sealed interface covers all cases");
    }

    public static void main(String[] args) {
        Shape circle = new Circle(3);
        Shape rectangle = new Rectangle(4, 5);
        Shape triangle = new Triangle(6, 2);

        System.out.println("area(Circle(r=3)): " + area(circle));
        System.out.println("area(Rectangle(4x5)): " + area(rectangle));
        System.out.println("area(Triangle(base=6, height=2)): " + area(triangle));

        // Records get equals()/hashCode()/toString() for free, generated from their components.
        System.out.println("Circle(3).toString(): " + circle);
        System.out.println("new Circle(3).equals(new Circle(3)): " + circle.equals(new Circle(3)));
        System.out.println("new Circle(3).equals(new Circle(4)): " + circle.equals(new Circle(4)));

        // sealed means the compiler knows this "permits" list is exhaustive -- e.g. a switch
        // pattern-matching over Shape (JDK 21+) wouldn't need a default branch. On 17 we can only
        // reach each case via instanceof, as area() does above, but the *exhaustiveness guarantee*
        // is already in force: nothing outside Circle/Rectangle/Triangle can implement Shape.
        System.out.println("Shape permits exactly: Circle, Rectangle, Triangle (enforced at compile time)");
    }
}
