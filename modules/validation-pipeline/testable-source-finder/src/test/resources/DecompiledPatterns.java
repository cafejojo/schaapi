import org.cafejojo.dummysimplemavenlibrary.Calculator;

public class Patterns {
    public Patterns() {
    }

    public static int pattern0(int var0) {
        Calculator var1 = new Calculator();
        int var2 = var1.sum(var0, 5);
        return var1.sum(var2, 2);
    }
}
