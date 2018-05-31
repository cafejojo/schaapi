package org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.users.ifconditional;

import org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

public class IfFalseUseTest {
    public Object1 test() {
        Object1 o1 = new Object1();

        if (Math.random() > 123) {
            new String("Cool input string");
        } else {
            o1.m1();
        }

        return o1;
    }
}
