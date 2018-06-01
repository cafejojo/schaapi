package org.cafejojo.schaapi.test;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class MyClass {
    public String foo() {
        return "bar";
    }

    public void qok() throws ZipException {
        new ZipFile("dinges").extractAll("danges");
    }
}
