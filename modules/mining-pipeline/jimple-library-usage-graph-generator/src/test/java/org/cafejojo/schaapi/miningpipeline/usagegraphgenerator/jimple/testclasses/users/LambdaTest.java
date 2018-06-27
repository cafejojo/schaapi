package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.users;

import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

import java.util.ArrayList;
import java.util.List;

public class LambdaTest {
    public void test() {
        List<Object1> o1s = new ArrayList<>();
        o1s.add(new Object1());

        o1s.forEach(o1 -> {
            o1.m1();
            o1.m3();
        });
    }
}
