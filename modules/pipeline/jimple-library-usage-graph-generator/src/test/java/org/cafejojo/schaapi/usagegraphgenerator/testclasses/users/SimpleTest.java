package org.cafejojo.schaapi.usagegraphgenerator.testclasses.users;

import org.cafejojo.schaapi.usagegraphgenerator.testclasses.library.Object1;

public class SimpleTest {
    public Object1 test() {
        String a = new String("a");
        String b = new String("b");
        Object1 o = new Object1();

        o.m2(b);

        a.substring(1);

        return o;
    }
}
