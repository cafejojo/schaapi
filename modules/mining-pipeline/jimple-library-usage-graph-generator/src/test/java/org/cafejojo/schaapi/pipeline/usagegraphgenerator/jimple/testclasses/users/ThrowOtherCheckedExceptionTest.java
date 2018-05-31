package org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.users;

import java.io.IOException;

public class ThrowOtherCheckedExceptionTest {
    public void foo() throws IOException {
        throw new IOException();
    }
}
