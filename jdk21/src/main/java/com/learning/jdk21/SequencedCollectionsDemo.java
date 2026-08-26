package com.learning.jdk21;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.TreeMap;

/**
 * JDK 21 (JEP 431): SequencedCollection, SequencedSet, SequencedMap. Before
 * 21, "does this collection have a defined order?" and "give me the first
 * and last element" were both List-only concepts -- Set and Map had no
 * common way to express it, even for naturally-ordered implementations like
 * LinkedHashSet/LinkedHashMap/TreeMap. These three new interfaces retrofit
 * a uniform contract onto all of them.
 */
public class SequencedCollectionsDemo {

    public List<Integer> firstAndLast(SequencedCollection<Integer> collection) {
        return List.of(collection.getFirst(), collection.getLast());
    }

    public List<Integer> reversedCopy(SequencedCollection<Integer> collection) {
        return new ArrayList<>(collection.reversed());
    }

    public static void main(String[] args) {
        sequencedCollectionOnAList();
        sequencedSetOnLinkedHashSet();
        sequencedMapOnLinkedHashMapAndTreeMap();
        reversedViewIsLiveNotACopy();
    }

    private static void sequencedCollectionOnAList() {
        SequencedCollectionsDemo demo = new SequencedCollectionsDemo();
        List<Integer> numbers = new ArrayList<>(List.of(10, 20, 30, 40));

        // Before 21: numbers.get(0) / numbers.get(numbers.size() - 1). Now: getFirst()/getLast(),
        // and it works the same way across every SequencedCollection, not just List.
        System.out.println("getFirst()/getLast() via firstAndLast(...): " + demo.firstAndLast(numbers));
        System.out.println("reversed() via reversedCopy(...): " + demo.reversedCopy(numbers));

        numbers.addFirst(5);   // new default method: prepend
        numbers.addLast(50);   // new default method: append (same as add(), spelled uniformly)
        System.out.println("after addFirst(5), addLast(50): " + numbers);
        System.out.println("removeFirst()/removeLast() are also available (mutating, List only shown here)");
    }

    private static void sequencedSetOnLinkedHashSet() {
        // LinkedHashSet has always had a stable iteration order (insertion order) -- it just had no
        // interface capturing that. Now it implements SequencedSet.
        SequencedSet<String> visited = new LinkedHashSet<>();
        visited.add("home");
        visited.add("products");
        visited.add("cart");
        visited.add("checkout");

        System.out.println("LinkedHashSet as a SequencedSet -- getFirst(): " + visited.getFirst());
        System.out.println("getLast(): " + visited.getLast());
        System.out.println("reversed(): " + visited.reversed());
    }

    private static void sequencedMapOnLinkedHashMapAndTreeMap() {
        SequencedMap<String, Integer> insertionOrdered = new LinkedHashMap<>();
        insertionOrdered.put("first", 1);
        insertionOrdered.put("second", 2);
        insertionOrdered.put("third", 3);

        // firstEntry()/lastEntry() return a Map.Entry directly -- no more
        // map.entrySet().iterator().next() dance just to peek at the first pair.
        System.out.println("LinkedHashMap firstEntry(): " + insertionOrdered.firstEntry());
        System.out.println("LinkedHashMap lastEntry(): " + insertionOrdered.lastEntry());
        System.out.println("LinkedHashMap sequencedKeySet(): " + insertionOrdered.sequencedKeySet());
        System.out.println("LinkedHashMap reversed(): " + insertionOrdered.reversed());

        // TreeMap was already sorted-order; it now exposes that same contract via SequencedMap too.
        SequencedMap<String, Integer> sorted = new TreeMap<>(insertionOrdered);
        System.out.println("TreeMap (sorted by key) firstEntry(): " + sorted.firstEntry());
        System.out.println("TreeMap (sorted by key) lastEntry(): " + sorted.lastEntry());
    }

    private static void reversedViewIsLiveNotACopy() {
        List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3));
        List<Integer> reversedView = numbers.reversed();

        System.out.println("reversed() view before mutation: " + reversedView);
        numbers.add(4); // mutate the ORIGINAL list...
        System.out.println("original list after add(4): " + numbers);
        System.out.println("reversed() view reflects it automatically (it's a live view, not a copy): "
                + reversedView);
    }
}
