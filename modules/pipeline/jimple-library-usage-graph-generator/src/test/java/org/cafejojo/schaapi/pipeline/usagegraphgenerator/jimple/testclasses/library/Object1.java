package org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.library;

public class Object1 {
    public int p1 = 123;

    public static void staticMethod() {

    }

    public double m1() {
        return Math.random();
    }

    public void m2(String input) {

    }

    public void m3() {

    }

    public void m4() {
        throw new LibraryException();
    }
}
