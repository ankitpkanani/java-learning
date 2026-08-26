// JDK 25 (JEP 512, finalized): Compact Source Files and Instance Main Methods.
//
// This file deliberately has NO package declaration and NO enclosing class --
// both are required for a "compact source file": javac rejects a package
// declaration here (confirmed empirically -- "compact source file should not
// have package declaration"), which is why this file lives directly under
// src/main/java instead of under com/learning/jdk25 like every other demo in
// this module.
//
// The compiler treats every top-level member below as belonging to one
// implicitly declared class named after the file, and main() may be an
// *instance* method -- no "public static", no String[] parameter required.
// This works with a normal javac compile (as Gradle does here), not only
// with the single-file "java CompactSourceMainDemo.java" source-launcher mode.

int callCount = 0; // an ordinary instance field on the implicit class

void main() {
    System.out.println("instance main(), no args, no enclosing class declaration");

    // "import module java.base;" is implicitly in effect for compact source files (JEP 511 synergy),
    // so java.util.List/Map/etc. are usable here with zero explicit import statements anywhere in
    // this file.
    List<String> jeps = List.of("JEP 511 (module imports)", "JEP 512 (this feature)");
    System.out.println("used without any import statement: " + jeps);

    greet("learner");
    greet("learner"); // call again to show the instance field persisting across calls

    System.out.println("callCount after two greet(...) calls: " + callCount);
}

// An ordinary top-level instance method -- callable from main() (and from each other) as if they
// were all methods of one class, because that's exactly what the compiler generates.
void greet(String name) {
    callCount++;
    System.out.println("hello, " + name + " (call #" + callCount + ")");
}
