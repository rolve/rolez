package ch.trick17.peppl.lib.util;

public class Math {
    
    public static int gcd(int a, int b) {
        while(b != 0) {
            final int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }
}
