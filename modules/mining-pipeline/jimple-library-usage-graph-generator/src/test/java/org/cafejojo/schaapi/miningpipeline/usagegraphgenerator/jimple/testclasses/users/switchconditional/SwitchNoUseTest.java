package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.users.switchconditional;

import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.Object1;

public class SwitchNoUseTest {
    public Object1 test() {
        Object1 o1 = new Object1();

        switch (o1.hashCode()) {
            case 1:
                int a = 1;
                break;
            case 2:
                int b = 2;
                break;
            default:
                int c = 3;
                break;
        }

        return o1;
    }
}
