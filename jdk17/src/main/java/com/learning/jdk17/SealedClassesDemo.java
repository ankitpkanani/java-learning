package com.learning.jdk17;

/**
 * JDK 17 (JEP 409): sealed classes/interfaces. Shape.java shows a sealed
 * *interface* whose permitted subtypes are all records. This demo shows the
 * sealed *class* case, where every permitted direct subclass must declare
 * exactly one of three modifiers: final, sealed, or non-sealed -- there is no
 * "just leave it open" option, unlike a normal (non-sealed) class hierarchy.
 */
public class SealedClassesDemo {

    abstract static sealed class Vehicle permits Car, Motorcycle, ExperimentalVehicle {
        abstract String describe();
    }

    // final: closes this branch of the hierarchy completely -- Car can never be subclassed.
    static final class Car extends Vehicle {
        @Override
        String describe() {
            return "Car (final -- cannot be extended further)";
        }
    }

    // sealed: still restricted, but re-opens a *new*, separately controlled permits list.
    abstract static sealed class Motorcycle extends Vehicle permits SportsBike, CruiserBike {
        @Override
        String describe() {
            return "Motorcycle (sealed -- only SportsBike/CruiserBike may extend it)";
        }
    }

    static final class SportsBike extends Motorcycle {
    }

    static final class CruiserBike extends Motorcycle {
    }

    // non-sealed: opts back into ordinary, unrestricted extensibility from this point down.
    static non-sealed class ExperimentalVehicle extends Vehicle {
        @Override
        String describe() {
            return "ExperimentalVehicle (non-sealed -- anyone can extend this)";
        }
    }

    // Legal precisely BECAUSE ExperimentalVehicle opted out of sealing with "non-sealed".
    static class JetPack extends ExperimentalVehicle {
        @Override
        String describe() {
            return "JetPack (an unforeseen subclass of the non-sealed ExperimentalVehicle)";
        }
    }

    public static void main(String[] args) {
        Vehicle[] vehicles = {new Car(), new SportsBike(), new CruiserBike(), new JetPack()};
        for (Vehicle v : vehicles) {
            System.out.println(v.getClass().getSimpleName() + " -> " + v.describe());
        }

        System.out.println();
        System.out.println("Vehicle permits: Car (final), Motorcycle (sealed), ExperimentalVehicle (non-sealed)");
        System.out.println("Motorcycle permits: SportsBike (final), CruiserBike (final)");
        System.out.println("ExperimentalVehicle: not sealed, so JetPack (and anything else) may extend it freely");

        // What sealing actually buys you: every permitted subtype must be known to the compiler at
        // compile time (same module, or same package for non-modular code) -- there is no equivalent
        // of "some subclass I've never heard of, defined in a jar I didn't compile against".
    }
}
