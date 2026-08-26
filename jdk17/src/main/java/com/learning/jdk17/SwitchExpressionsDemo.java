package com.learning.jdk17;

/**
 * JDK 14 (JEP 361): switch expressions. A switch can now be an *expression*
 * that produces a value, using "case L ->" arrow syntax that doesn't fall
 * through, plus "yield" for producing a value from a block, and multiple
 * case labels on one line.
 */
public class SwitchExpressionsDemo {

    enum Day {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    public static void main(String[] args) {
        classicStatementSwitch();
        arrowExpressionSwitch();
        multipleLabelsPerCase();
        yieldFromABlock();
        exhaustiveEnumSwitchNoDefaultNeeded();
    }

    private static void classicStatementSwitch() {
        int day = 3;
        String name;
        // Old style: colon labels, explicit break, and switch is a *statement* -- it doesn't
        // produce a value, so we assign to "name" inside each branch and must remember every break.
        switch (day) {
            case 1:
                name = "Monday";
                break;
            case 2:
                name = "Tuesday";
                break;
            case 3:
                name = "Wednesday";
                break;
            default:
                name = "Some other day";
                break;
        }
        System.out.println("classic statement switch: " + name);
    }

    private static void arrowExpressionSwitch() {
        int day = 3;
        // New style: switch IS the expression. Each arm is a single expression (or a block ending in
        // yield), there's no fall-through, and no break needed.
        String name = switch (day) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            default -> "Some other day";
        };
        System.out.println("arrow expression switch: " + name);
    }

    private static void multipleLabelsPerCase() {
        for (Day d : Day.values()) {
            String kind = switch (d) {
                case SATURDAY, SUNDAY -> "weekend";
                case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "weekday";
            };
            System.out.println(d + " -> " + kind);
        }
    }

    private static void yieldFromABlock() {
        int score = 73;
        // An arrow arm can be a block instead of a single expression, as long as it "yield"s a value.
        String grade = switch (score / 10) {
            case 10, 9 -> "A";
            case 8 -> "B";
            case 7 -> {
                String note = score >= 75 ? "high C" : "low C";
                yield "C (" + note + ")";
            }
            default -> "F";
        };
        System.out.println("score " + score + " -> grade " + grade);
    }

    private static void exhaustiveEnumSwitchNoDefaultNeeded() {
        // Because Day is an enum (a closed set of values, much like sealed types), the compiler can
        // verify every constant is covered and does NOT require a default branch here at all.
        for (Day d : Day.values()) {
            int isoIndex = switch (d) {
                case MONDAY -> 1;
                case TUESDAY -> 2;
                case WEDNESDAY -> 3;
                case THURSDAY -> 4;
                case FRIDAY -> 5;
                case SATURDAY -> 6;
                case SUNDAY -> 7;
            };
            System.out.println(d + " ISO index: " + isoIndex);
        }
    }
}
