package ch.trick17.peppl.lib.guard;

public class GuardedIntArray extends GuardedIntSlice {
    
    @SafeVarargs
    public GuardedIntArray(final int... data) {
        super(data, 0, data.length);
    }
}
