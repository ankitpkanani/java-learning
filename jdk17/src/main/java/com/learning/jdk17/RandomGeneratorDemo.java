package com.learning.jdk17;

import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;

/**
 * JDK 17 (JEP 356): enhanced pseudo-random number generators. Before 17,
 * java.util.Random was the common base type, but it's a single fixed
 * algorithm with a synchronized internal seed (bad for concurrent use).
 * RandomGenerator is a new interface implemented by Random, SecureRandom,
 * ThreadLocalRandom, AND a family of new, pluggable algorithms -- so code can
 * depend on the interface and pick an algorithm by name at runtime.
 */
public class RandomGeneratorDemo {

    public static void main(String[] args) {
        existingClassesNowImplementRandomGenerator();
        listAvailableAlgorithms();
        pickAnAlgorithmByName();
        streamsOfRandomValues();
        jumpableForParallelStreams();
    }

    private static void existingClassesNowImplementRandomGenerator() {
        // java.util.Random, SecureRandom, and ThreadLocalRandom all implement RandomGenerator now,
        // so code written against the interface works with any of them interchangeably.
        RandomGenerator legacy = new java.util.Random(42);
        RandomGenerator threadLocal = java.util.concurrent.ThreadLocalRandom.current();

        System.out.println("java.util.Random implements RandomGenerator: " + (legacy instanceof RandomGenerator));
        System.out.println("ThreadLocalRandom implements RandomGenerator: " + (threadLocal instanceof RandomGenerator));
        System.out.println("legacy.nextInt(100) (seeded, deterministic): " + legacy.nextInt(100));
    }

    private static void listAvailableAlgorithms() {
        // The JDK ships several algorithms beyond the classic LCG that backs java.util.Random --
        // e.g. Xoshiro256PlusPlus, L64X128MixRandom -- each with different speed/quality/statistical
        // tradeoffs, all discoverable by name via RandomGeneratorFactory.
        Set<String> names = RandomGeneratorFactory.all()
                .map(RandomGeneratorFactory::name)
                .collect(Collectors.toCollection(java.util.TreeSet::new));
        System.out.println("RandomGeneratorFactory.all() algorithm names: " + names);
    }

    private static void pickAnAlgorithmByName() {
        RandomGeneratorFactory<RandomGenerator> factory = RandomGeneratorFactory.of("Xoshiro256PlusPlus");
        RandomGenerator generator = factory.create(42L);

        System.out.println("factory.of(\"Xoshiro256PlusPlus\").create(42L).nextInt(100): " + generator.nextInt(100));
        System.out.println("algorithm group: " + factory.group());
        System.out.println("factory.isStatistical(): " + factory.isStatistical());
    }

    private static void streamsOfRandomValues() {
        RandomGenerator generator = RandomGeneratorFactory.of("L64X128MixRandom").create(7L);

        // RandomGenerator exposes stream-producing methods directly -- no manual loop needed.
        int[] tenRolls = generator.ints(10, 1, 7).toArray(); // 10 values in [1, 7)
        double average = generator.doubles(1000).average().orElse(0);

        System.out.println("generator.ints(10, 1, 7).toArray() (10 dice rolls): " + java.util.Arrays.toString(tenRolls));
        System.out.println("average of 1000 generator.doubles() (expect close to 0.5): " + average);
    }

    private static void jumpableForParallelStreams() {
        // Some algorithms implement RandomGenerator.JumpableGenerator: jump() advances the state as
        // if a huge number of values had been drawn, in O(1) -- a fast way to hand each parallel
        // worker its own non-overlapping stream of "randomness" from one seed.
        RandomGenerator.JumpableGenerator generator =
                (RandomGenerator.JumpableGenerator) RandomGeneratorFactory.of("Xoshiro256PlusPlus").create(1L);

        int before = generator.nextInt();
        generator.jump();
        int afterJump = generator.nextInt();

        System.out.println("nextInt() before jump(): " + before);
        System.out.println("nextInt() after jump()  (unrelated stream of values): " + afterJump);
    }
}
