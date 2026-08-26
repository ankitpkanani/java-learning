package com.learning.jdk8;

import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * JDK 8: java.util.stream.Collectors -- reusable, composable terminal
 * operations for turning a Stream into a collection, a String, a summary, or
 * a grouped/partitioned Map.
 */
public class CollectorsDemo {

    // A plain class, not a record -- records are a JDK 16 feature and this
    // module is compiled for JDK 8.
    static final class Employee {
        private final String name;
        private final String department;
        private final int salary;

        Employee(String name, String department, int salary) {
            this.name = name;
            this.department = department;
            this.salary = salary;
        }

        String name() {
            return name;
        }

        String department() {
            return department;
        }

        int salary() {
            return salary;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
                new Employee("Alice", "Engineering", 95000),
                new Employee("Bob", "Engineering", 87000),
                new Employee("Carla", "Sales", 60000),
                new Employee("Dave", "Sales", 65000),
                new Employee("Eve", "Marketing", 72000)
        );

        toListAndToSet(employees);
        joining(employees);
        groupingBy(employees);
        groupingByWithDownstreamCollector(employees);
        partitioningBy(employees);
        toMap(employees);
        summarizing(employees);
    }

    private static void toListAndToSet(List<Employee> employees) {
        List<String> names = employees.stream().map(Employee::name).collect(Collectors.toList());
        Set<String> departments = employees.stream()
                .map(Employee::department)
                .collect(Collectors.toCollection(TreeSet::new)); // sorted set, via a supplier
        System.out.println("Collectors.toList() names: " + names);
        System.out.println("Collectors.toCollection(TreeSet::new) departments: " + departments);
    }

    private static void joining(List<Employee> employees) {
        String csv = employees.stream()
                .map(Employee::name)
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("Collectors.joining(\", \", \"[\", \"]\"): " + csv);
    }

    private static void groupingBy(List<Employee> employees) {
        Map<String, List<Employee>> byDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::department));
        System.out.println("Collectors.groupingBy(Employee::department):");
        byDepartment.forEach((dept, staff) -> System.out.println("  " + dept + " -> " + staff));
    }

    private static void groupingByWithDownstreamCollector(List<Employee> employees) {
        // groupingBy accepts a *downstream* collector to fold each group further.
        Map<String, Long> countByDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::department, Collectors.counting()));
        Map<String, Double> avgSalaryByDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::department, Collectors.averagingInt(Employee::salary)));
        Map<String, List<String>> namesByDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::department, Collectors.mapping(Employee::name, Collectors.toList())));

        System.out.println("groupingBy + counting(): " + countByDepartment);
        System.out.println("groupingBy + averagingInt(salary): " + avgSalaryByDepartment);
        System.out.println("groupingBy + mapping(name, toList()): " + namesByDepartment);
    }

    private static void partitioningBy(List<Employee> employees) {
        // partitioningBy always yields exactly two buckets: Map<Boolean, List<T>>.
        Map<Boolean, List<Employee>> bySalaryThreshold = employees.stream()
                .collect(Collectors.partitioningBy(e -> e.salary() >= 70000));
        System.out.println("Collectors.partitioningBy(salary >= 70000):");
        System.out.println("  true  -> " + bySalaryThreshold.get(true));
        System.out.println("  false -> " + bySalaryThreshold.get(false));
    }

    private static void toMap(List<Employee> employees) {
        Map<String, Integer> salaryByName = employees.stream()
                .collect(Collectors.toMap(Employee::name, Employee::salary));
        System.out.println("Collectors.toMap(name, salary): " + salaryByName);
    }

    private static void summarizing(List<Employee> employees) {
        IntSummaryStatistics stats = employees.stream().collect(Collectors.summarizingInt(Employee::salary));
        System.out.printf(
                "Collectors.summarizingInt(salary): count=%d, min=%d, max=%d, sum=%d, avg=%.2f%n",
                stats.getCount(), stats.getMin(), stats.getMax(), stats.getSum(), stats.getAverage());
    }
}
