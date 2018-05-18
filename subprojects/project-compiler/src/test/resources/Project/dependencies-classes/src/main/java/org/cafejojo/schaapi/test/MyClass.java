package org.cafejojo.schaapi.test;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;

public class MyClass {
    private Integer constant = 20000;

    public String foo() throws ZipException {
        new ZipFile(new File("asdf"));
        String myString = "asdfasdfasdf";
        String yourString = myString.substring(constant);
        String ourString = myString.substring(1).substring(3)
            + yourString;
        return ourString;
    }
}
