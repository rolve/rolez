package ch.trick17.peppl.lib;

public class GuardFactory {
    
    private static volatile GuardFactory defaultFactory = new GuardFactory();
    
    public static GuardFactory getDefault() {
        return defaultFactory;
    }
    
    public Guard newGuard() {
        return new Guard();
    }
}
