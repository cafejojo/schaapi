package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.users;

import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.LibrarySupplier;
import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

public class LambdaClassConstantTest {
    public void test() {
        LibrarySupplier<Object1> supplier = Object1::staticMethod2;
    }
}
