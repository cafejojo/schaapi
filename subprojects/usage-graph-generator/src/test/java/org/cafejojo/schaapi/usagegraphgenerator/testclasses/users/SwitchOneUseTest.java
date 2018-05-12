package org.cafejojo.schaapi.usagegraphgenerator.testclasses.users;

import org.cafejojo.schaapi.usagegraphgenerator.testclasses.library.Object1;

public class SwitchOneUseTest {
    public void test() {
        Object1 o1 = new Object1();

        switch (o1.hashCode()) {
            case 1:
                int a = 1;
                break;
            case 2:
                int b = 2;
                o1.m1();
                break;
            case 3:
                int c = 3;
                break;
        }
    }
}
