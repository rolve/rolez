package ch.trick17.peppl.lib.guard;

import java.util.Arrays;

public class IntArray extends IntSlice {
    
    @SafeVarargs
    public IntArray(final int... data) {
        super(data, 0, data.length);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}
