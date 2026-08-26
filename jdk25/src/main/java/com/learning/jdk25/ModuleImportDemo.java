package com.learning.jdk25;

// JDK 25: Module Import Declarations (JEP 511, finalized) let you import every
// exported package of a module in one line instead of listing each package.
import module java.base;
// A module import can be combined with (or overridden by) ordinary imports of
// other modules -- here, java.net.http isn't part of java.base.
import module java.net.http;

public class ModuleImportDemo {

    public List<String> upperCased(List<String> values) {
        return values.stream().map(String::toUpperCase).toList();
    }

    public static void main(String[] args) throws Exception {
        typesFromAcrossJavaBaseWithNoExplicitImports();
        importedFromASecondModule();
        explicitImportStillWins();
    }

    private static void typesFromAcrossJavaBaseWithNoExplicitImports() {
        // Before 25: this file would need "import java.util.List;", "import java.util.Map;",
        // "import java.util.Optional;", "import java.time.Duration;", "import java.nio.file.Path;" --
        // five separate lines for five different java.base packages. "import module java.base;"
        // covers all of java.base's exported packages at once.
        List<Integer> list = List.of(1, 2, 3);
        Map<String, Integer> map = Map.of("a", 1, "b", 2);
        Optional<String> optional = Optional.of("present");
        Duration duration = Duration.ofSeconds(90);
        Path path = Path.of("some", "relative", "path.txt");

        System.out.println("List (java.util): " + list);
        System.out.println("Map (java.util): " + map);
        System.out.println("Optional (java.util): " + optional);
        System.out.println("Duration (java.time): " + duration);
        System.out.println("Path (java.nio.file): " + path);

        ModuleImportDemo demo = new ModuleImportDemo();
        System.out.println("upperCased(...): " + demo.upperCased(List.of("ada", "grace")));
    }

    private static void importedFromASecondModule() throws Exception {
        // HttpClient/HttpRequest/HttpResponse live in the java.net.http module, not java.base --
        // "import module java.net.http;" pulls in that module's exported packages the same way.
        HttpClient client = HttpClient.newHttpClient();
        System.out.println("HttpClient from 'import module java.net.http': " + client.version());
    }

    private static void explicitImportStillWins() {
        // If a type name is ambiguous across imported modules, or you simply want to be explicit
        // about exactly where a type comes from, an ordinary single-type import can still be added
        // alongside module imports -- they compose rather than conflict.
        System.out.println("module imports are just import declarations -- ordinary imports still work fine");
    }
}
