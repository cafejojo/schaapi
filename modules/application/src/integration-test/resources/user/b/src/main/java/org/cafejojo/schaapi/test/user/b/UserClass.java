package org.cafejojo.schaapi.test.user.b;

import org.cafejojo.schaapi.test.library.LibraryClass;

public class UserClass {
    public int userFoo() {
        int x = new LibraryClass().libraryFoo();
        return x;
    }

    public int userBaz() {
        int x = new LibraryClass().libraryFoo();
        if (x > 0) {
            System.out.println(new LibraryClass().libraryFoo() + x);
        }
        return x;
    }
}
