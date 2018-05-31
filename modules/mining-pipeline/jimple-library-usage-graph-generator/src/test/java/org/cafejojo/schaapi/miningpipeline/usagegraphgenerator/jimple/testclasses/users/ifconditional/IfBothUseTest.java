package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.users.ifconditional;

import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

public class IfBothUseTest {
    public Object1 test() {
        Object1 o1 = new Object1();

        if (Math.random() > 123) {
            o1.m1();
        } else {
            o1.m3();
        }

        return o1;
    }
}
