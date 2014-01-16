package ch.trick17.peppl.lib.guard;

public class IntArray extends IntSlice {
    
    @SafeVarargs
    public IntArray(final int... data) {
        super(data, 0, data.length);
    }
}
