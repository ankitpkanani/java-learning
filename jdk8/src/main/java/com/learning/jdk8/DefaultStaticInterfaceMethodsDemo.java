package com.learning.jdk8;

/**
 * JDK 8: default methods let an interface provide a method body, so new
 * methods can be added to an interface without breaking every existing
 * implementer (this is what made the Collection.stream() retrofit possible).
 * Static methods on interfaces give a home for related factory/utility logic.
 */
public class DefaultStaticInterfaceMethodsDemo {

    interface Vehicle {

        String name();

        // Default method: implementers get this for free, but may override it.
        default String describe() {
            return name() + " is a vehicle";
        }

        // Static method: called as Vehicle.builtBy(...), not inherited by implementers.
        static Vehicle builtBy(String name, String maker) {
            return () -> name + " (by " + maker + ")";
        }
    }

    // Uses the default describe() as-is.
    static class Bicycle implements Vehicle {
        @Override
        public String name() {
            return "Bicycle";
        }
    }

    // Overrides the default.
    static class Car implements Vehicle {
        @Override
        public String name() {
            return "Car";
        }

        @Override
        public String describe() {
            return name() + " is a motor vehicle";
        }
    }

    // Diamond case: a class implementing two interfaces that both default the
    // same method signature MUST override it explicitly -- Java won't guess
    // which one you meant.
    interface Floats {
        default String mode() {
            return "floats";
        }
    }

    interface Flies {
        default String mode() {
            return "flies";
        }
    }

    static class FlyingBoat implements Floats, Flies {
        @Override
        public String mode() {
            // Explicitly pick (or combine) the conflicting defaults via InterfaceName.super.method().
            return Floats.super.mode() + " and " + Flies.super.mode();
        }
    }

    public static void main(String[] args) {
        Vehicle bicycle = new Bicycle();
        Vehicle car = new Car();
        System.out.println("Bicycle uses inherited default: " + bicycle.describe());
        System.out.println("Car overrides the default: " + car.describe());

        Vehicle fromFactory = Vehicle.builtBy("Model X", "Acme"); // calling the interface's static method
        System.out.println("Vehicle.builtBy(...): " + fromFactory.describe());

        FlyingBoat hybrid = new FlyingBoat();
        System.out.println("FlyingBoat resolves the diamond via Interface.super.mode(): " + hybrid.mode());
    }
}
