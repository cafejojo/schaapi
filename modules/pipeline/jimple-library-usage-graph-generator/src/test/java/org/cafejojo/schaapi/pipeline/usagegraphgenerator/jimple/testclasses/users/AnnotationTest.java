package org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.users;

import org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

public class AnnotationTest {
    @Override
    public String toString() {
        return String.valueOf(new Object1().p1);
    }
}
