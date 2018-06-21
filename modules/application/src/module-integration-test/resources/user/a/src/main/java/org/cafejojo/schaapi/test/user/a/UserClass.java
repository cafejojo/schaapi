package org.cafejojo.schaapi.test.user.a;

import com.google.common.base.Charsets;
import org.cafejojo.schaapi.test.library.LibraryClass;

import java.nio.charset.Charset;

public class UserClass {
    public int userFoo() {
        int x = new LibraryClass().libraryFoo();
        Charset utf8 = Charsets.UTF_8;
        return x;
    }

    public int userBar() {
        System.out.println(new LibraryClass().foo);
        return 922;
    }
}
