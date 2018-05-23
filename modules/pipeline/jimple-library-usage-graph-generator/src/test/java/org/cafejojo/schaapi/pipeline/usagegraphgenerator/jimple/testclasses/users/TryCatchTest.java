package org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.users;

import org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.library.LibraryException;
import org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

public class TryCatchTest {
    public void test() {
        try {
            new Object1().m1();
        } catch (LibraryException e) {
            System.out.println("Test");
        }
    }
}
