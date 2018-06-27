package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.users;

import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

import java.util.function.Supplier;

public class LambdaTest {
    public void test() {
        Supplier<Object1> supplier = Object1::staticMethod2;
    }
}
