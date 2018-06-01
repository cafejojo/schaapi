package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.users;

import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

public class ThrowTest {
    public void test() {
        throw new RuntimeException(String.valueOf(new Object1().m1()));
    }
}
