package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.users.ifconditional;

import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

public class IfNoUseTest {
    public Object1 test() {
        Object1 o1 = new Object1();

        if (Math.random() > 123) {
            new String("Cool input string 1");
        } else {
            new String("Cool input string 2");
        }

        return o1;
    }
}
