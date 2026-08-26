package com.learning.jdk25;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.FieldModel;
import java.lang.classfile.MethodModel;
import java.lang.reflect.AccessFlag;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;

/**
 * JDK 24 (JEP 484, finalized -- carried forward into 25): the Class-File API,
 * java.lang.classfile. A standard, JDK-maintained library for parsing,
 * generating, and transforming .class files -- previously the domain of
 * third-party bytecode libraries like ASM, which historically had to release
 * new versions in lockstep with every JDK class-file format change.
 */
public class ClassFileApiDemo {

    // A small, self-contained target class for the demo to introspect -- deliberately not itself,
    // so the output is a short, easy-to-read structure.
    static class Sample {
        private final int value;

        Sample(int value) {
            this.value = value;
        }

        int doubled() {
            return value * 2;
        }
    }

    public static void main(String[] args) throws Exception {
        Path classFile = locateCompiledClassFile();
        ClassModel model = ClassFile.of().parse(classFile);

        printBasicClassInfo(model);
        printFields(model);
        printMethods(model);
    }

    private static Path locateCompiledClassFile() throws URISyntaxException {
        // Find Sample.class next to this demo's own .class file on the runtime classpath -- works
        // whether run from Gradle's build/classes output or a packaged jar's exploded location.
        String resourceName = Sample.class.getName().replace('.', '/') + ".class";
        var url = ClassFileApiDemo.class.getClassLoader().getResource(resourceName);
        if (url == null) {
            throw new IllegalStateException("could not locate " + resourceName + " on the classpath");
        }
        return Path.of(url.toURI());
    }

    private static void printBasicClassInfo(ClassModel model) {
        System.out.println("class name: " + model.thisClass().asInternalName());
        System.out.println("superclass: " + model.superclass().map(sc -> sc.asInternalName()).orElse("<none>"));
        System.out.println("major version (class file format): " + model.majorVersion());
        Set<AccessFlag> flags = model.flags().flags();
        System.out.println("access flags: " + flags);
    }

    private static void printFields(ClassModel model) {
        System.out.println("fields:");
        for (FieldModel field : model.fields()) {
            System.out.println("  " + field.fieldTypeSymbol().displayName() + " " + field.fieldName().stringValue()
                    + " " + field.flags().flags());
        }
    }

    private static void printMethods(ClassModel model) {
        System.out.println("methods:");
        for (MethodModel method : model.methods()) {
            System.out.println("  " + method.methodName().stringValue() + method.methodTypeSymbol().displayDescriptor()
                    + " " + method.flags().flags());
        }
    }
}
