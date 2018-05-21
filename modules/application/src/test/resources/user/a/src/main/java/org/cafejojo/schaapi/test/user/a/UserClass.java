package org.cafejojo.schaapi.test.user.a;

import org.cafejojo.schaapi.test.library.LibraryClass;

public class UserClass {
    public int userFoo() {
        int x = new LibraryClass().libraryFoo();
        return x;
    }
}
