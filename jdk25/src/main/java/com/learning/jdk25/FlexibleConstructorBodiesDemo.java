package com.learning.jdk25;

/**
 * JDK 24 (JEP 492, finalized -- originally previewed as JEP 447/482, carried
 * forward into 25): flexible constructor bodies. Before this, super(...)/
 * this(...) had to be the very first statement in a constructor, full stop --
 * even a simple argument-validation "if" had to be squeezed into the
 * super(...) call's arguments themselves, or done in a separate static
 * helper method. Now ordinary statements can run BEFORE the super()/this()
 * call, as long as they don't touch the instance being constructed.
 */
public class FlexibleConstructorBodiesDemo {

    static class Account {
        final double balance;

        Account(double balance) {
            this.balance = balance;
            System.out.println("Account(" + balance + ") constructed");
        }
    }

    static class ValidatedAccount extends Account {
        ValidatedAccount(double initialDeposit) {
            // Statements before super(...) are now allowed -- as long as they don't read/write
            // "this" or call an instance method, since the object isn't "born" yet at this point.
            if (initialDeposit < 0) {
                throw new IllegalArgumentException("initialDeposit cannot be negative: " + initialDeposit);
            }
            double normalized = Math.max(initialDeposit, 0.01); // pre-processing, still pre-super()

            super(normalized); // must still be called eventually, just no longer required to be first

            System.out.println("ValidatedAccount finished construction with normalized balance");
        }
    }

    static class LoggingAccount extends Account {
        private static int instancesCreated = 0;

        LoggingAccount(double balance) {
            // Reading/writing a STATIC field before super() is fine -- statics belong to the class,
            // not to the not-yet-constructed instance.
            instancesCreated++;
            System.out.println("about to construct LoggingAccount #" + instancesCreated);

            super(balance);
        }
    }

    public static void main(String[] args) {
        validationBeforeSuper();
        preprocessingBeforeSuper();
        staticStateBeforeSuper();
        whatStillIsNotAllowed();
    }

    private static void validationBeforeSuper() {
        try {
            new ValidatedAccount(-50);
        } catch (IllegalArgumentException e) {
            System.out.println("negative deposit rejected before super() ran at all: " + e.getMessage());
        }
    }

    private static void preprocessingBeforeSuper() {
        ValidatedAccount account = new ValidatedAccount(0);
        System.out.println("ValidatedAccount(0) normalized balance: " + account.balance);
    }

    private static void staticStateBeforeSuper() {
        new LoggingAccount(100);
        new LoggingAccount(200);
        System.out.println("LoggingAccount.instancesCreated: " + LoggingAccount.instancesCreated);
    }

    private static void whatStillIsNotAllowed() {
        // Still illegal, even under JEP 492 -- these would all be compile errors if uncommented,
        // because the instance doesn't exist until super()/this() actually runs:
        //
        //   class Bad extends Account {
        //       Bad(double balance) {
        //           this.balance = balance;      // ERROR: can't touch 'this' before super()
        //           System.out.println(toString()); // ERROR: implicit 'this' via instance method call
        //           super(balance);
        //       }
        //   }
        //
        System.out.println("still disallowed before super()/this(): touching 'this', "
                + "calling instance methods, reading instance fields");
    }
}
