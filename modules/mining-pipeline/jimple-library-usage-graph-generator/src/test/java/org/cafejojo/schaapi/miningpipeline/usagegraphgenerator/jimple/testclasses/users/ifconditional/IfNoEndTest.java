package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.users.ifconditional;

import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

public class IfNoEndTest {
    public int test() {
        Object1 o1 = new Object1();

        if (Math.random() > 123) {
            return 456;
        }

        return 123;
    }
}
