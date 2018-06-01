package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.users.ifconditional;

import java.util.concurrent.LinkedBlockingQueue;

public class IfReturnsTest {
    public Object test() {
        boolean a = true;
        if (a) {
            return new LinkedBlockingQueue();
        } else {
            return new LinkedBlockingQueue();
        }
    }
}
