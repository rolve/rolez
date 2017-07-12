package rolez.lang;

public class Assertion {
    
    public Assertion(boolean condition) {
        if(!condition)
            throw new AssertionError();
    }
    
    public Assertion(boolean condition, String message) {
        if(!condition)
            throw new AssertionError(message);
    }
}
