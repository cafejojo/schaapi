package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.users;

import java.util.ArrayList;
import java.util.List;

import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

public class ArrayListAndLambdaTest {
    public double test() {
        List<Object1> list = new ArrayList<>();
        list.add(new Object1());

        double result = list.stream().mapToDouble(Object1::m1).count();

        return result;
    }
}
