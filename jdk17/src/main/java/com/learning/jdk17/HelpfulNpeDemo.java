package com.learning.jdk17;

/**
 * JDK 14 (JEP 358), on by default since JDK 15: helpful NullPointerExceptions.
 * Instead of a bare "NullPointerException" with no detail, the JVM now
 * describes exactly which variable/field/array-element/method-return-value
 * was null, by decompiling the bytecode of the failing instruction.
 */
public class HelpfulNpeDemo {

    static class Address {
        String city;
    }

    static class Person {
        Address address;
        String[] nicknames;
    }

    public static void main(String[] args) {
        nullFieldChainDemo();
        nullArrayElementDemo();
        nullMethodReturnValueDemo();
    }

    private static void nullFieldChainDemo() {
        Person person = new Person(); // person.address was never assigned -- stays null

        try {
            // A chained field access: person.address.city. Without helpful NPEs, the message
            // would just be "NullPointerException" with no indication of which link broke.
            String city = person.address.city;
            System.out.println("unreachable: " + city);
        } catch (NullPointerException e) {
            System.out.println("chained field access failure:");
            System.out.println("  " + e.getMessage());
        }
    }

    private static void nullArrayElementDemo() {
        Person person = new Person();
        person.nicknames = new String[3]; // an array of nulls -- every slot is null by default

        try {
            int length = person.nicknames[1].length();
            System.out.println("unreachable: " + length);
        } catch (NullPointerException e) {
            System.out.println("null array element failure:");
            System.out.println("  " + e.getMessage());
        }
    }

    private static String findCityOrNull(Person person) {
        return null; // simulates a method that legitimately can return null
    }

    private static void nullMethodReturnValueDemo() {
        Person person = new Person();

        try {
            int length = findCityOrNull(person).length();
            System.out.println("unreachable: " + length);
        } catch (NullPointerException e) {
            System.out.println("null method-return-value failure:");
            System.out.println("  " + e.getMessage());
        }

        System.out.println();
        System.out.println("Helpful NPE messages are ON by default since JDK 15 (JEP 358);");
        System.out.println("on JDK 14 they required -XX:+ShowCodeDetailsInExceptionMessages.");
    }
}
