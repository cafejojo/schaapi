package org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.users;

import org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

public class LoopTest {
    public void test() {
        Object1 o1 = new Object1();

        for (int i = 0; i < o1.m1(); i++) {
            o1.m3();

            for (int j = 0; j < o1.m1(); j++) {
                o1.m3();
            }

            o1.m3();
        }
    }
}
