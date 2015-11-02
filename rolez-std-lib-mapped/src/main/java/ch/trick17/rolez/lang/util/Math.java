package ch.trick17.rolez.lang.util;

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
