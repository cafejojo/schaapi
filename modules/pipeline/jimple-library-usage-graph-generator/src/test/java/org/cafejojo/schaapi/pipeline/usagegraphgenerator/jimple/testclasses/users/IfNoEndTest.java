package org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.users;

import org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

public class IfNoEndTest {
    public int test() {
        Object1 o1 = new Object1();

        if (Math.random() > 123) {
            return 456;
        }

        return 123;
    }
}
