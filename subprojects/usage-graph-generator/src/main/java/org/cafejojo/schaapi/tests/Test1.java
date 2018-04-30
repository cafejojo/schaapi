package org.cafejojo.schaapi.tests;

import com.developers.library.Object1;

public class Test1 {
    public Object1 test() {
        Object1 o1 = new Object1();

        if (o1.m1() > o1.p1) {
            o1.m2(new String("Cool input string"));
        } else {
            o1.m3();
        }

        return o1;
    }
}
