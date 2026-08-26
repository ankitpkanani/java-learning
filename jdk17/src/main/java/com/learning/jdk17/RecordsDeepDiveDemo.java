package com.learning.jdk17;

import java.util.Objects;

/**
 * JDK 16 (JEP 395): records are compact, immutable data carriers. The
 * compiler generates a canonical constructor, private final fields,
 * accessors, equals()/hashCode()/toString() from the component list --
 * but you can still customize almost all of it.
 */
public class RecordsDeepDiveDemo {

    // Canonical form: components declared once, everything else generated.
    record Point(int x, int y) {}

    // A compact constructor: no parameter list, no explicit field assignment -- it runs
    // *before* the implicit field assignment, purely to validate/normalize the arguments.
    record Range(int min, int max) {
        Range {
            if (min > max) {
                throw new IllegalArgumentException("min (" + min + ") > max (" + max + ")");
            }
        }
    }

    // Records can declare extra constructors, but every one of them must ultimately delegate
    // to the canonical constructor via this(...).
    record Fraction(int numerator, int denominator) {
        Fraction {
            if (denominator == 0) {
                throw new ArithmeticException("denominator cannot be zero");
            }
            int gcd = gcd(Math.abs(numerator), Math.abs(denominator));
            if (gcd > 1) {
                numerator /= gcd;
                denominator /= gcd;
            }
        }

        // Convenience constructor for whole numbers.
        Fraction(int wholeNumber) {
            this(wholeNumber, 1);
        }

        private static int gcd(int a, int b) {
            return b == 0 ? Math.max(a, 1) : gcd(b, a % b);
        }

        // Extra instance method beyond the generated accessors -- perfectly normal on a record.
        double asDouble() {
            return (double) numerator / denominator;
        }
    }

    // Records can implement interfaces (just not extend another class -- they implicitly extend
    // java.lang.Record already, and Java has no multiple class inheritance).
    interface HasArea {
        double area();
    }

    record Square(double side) implements HasArea {
        @Override
        public double area() {
            return side * side;
        }
    }

    // A record can override a generated accessor -- e.g. to defensively copy a mutable component.
    record Inventory(String name, int[] stock) {
        @Override
        public int[] stock() {
            return stock.clone(); // never hand out the internal array itself
        }
    }

    public static void main(String[] args) {
        canonicalConstructorAndGeneratedMembers();
        compactConstructorValidation();
        extraConstructorsAndMethods();
        implementingAnInterface();
        defensiveAccessorOverride();
    }

    private static void canonicalConstructorAndGeneratedMembers() {
        Point p1 = new Point(3, 4);
        Point p2 = new Point(3, 4);
        Point p3 = new Point(5, 5);

        System.out.println("p1.toString() (generated): " + p1);
        System.out.println("p1.x() / p1.y() (generated accessors): " + p1.x() + ", " + p1.y());
        System.out.println("p1.equals(p2) (generated, component-wise): " + p1.equals(p2));
        System.out.println("p1.equals(p3): " + p1.equals(p3));
        System.out.println("p1.hashCode() == p2.hashCode() (generated, consistent with equals): "
                + (p1.hashCode() == p2.hashCode()));
    }

    private static void compactConstructorValidation() {
        Range valid = new Range(1, 10);
        System.out.println("new Range(1, 10): " + valid);
        try {
            new Range(10, 1);
        } catch (IllegalArgumentException e) {
            System.out.println("new Range(10, 1) rejected by the compact constructor: " + e.getMessage());
        }
    }

    private static void extraConstructorsAndMethods() {
        Fraction simplified = new Fraction(4, 8); // compact ctor reduces this to 1/2
        Fraction whole = new Fraction(5);          // extra ctor delegates to the canonical one
        System.out.println("new Fraction(4, 8) auto-simplifies to: " + simplified);
        System.out.println("new Fraction(5) via the extra ctor: " + whole);
        System.out.println("simplified.asDouble(): " + simplified.asDouble());
        try {
            new Fraction(1, 0);
        } catch (ArithmeticException e) {
            System.out.println("new Fraction(1, 0) rejected: " + e.getMessage());
        }
    }

    private static void implementingAnInterface() {
        HasArea shape = new Square(5);
        System.out.println("Square(5) implements HasArea, area(): " + shape.area());
    }

    private static void defensiveAccessorOverride() {
        int[] originalStock = {10, 20, 30};
        Inventory inventory = new Inventory("Widgets", originalStock);

        int[] exposed = inventory.stock();
        exposed[0] = 999; // mutate the array we got back
        System.out.println("mutating the array returned by stock() does NOT affect the record: "
                + Objects.toString(java.util.Arrays.toString(inventory.stock())));
        System.out.println("(exposed copy was mutated to " + java.util.Arrays.toString(exposed) + ", "
                + "original array in the record is untouched because stock() clones on the way out)");
    }
}
