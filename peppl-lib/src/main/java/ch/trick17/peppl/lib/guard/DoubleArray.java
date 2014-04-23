package ch.trick17.peppl.lib.guard;

import java.util.Arrays;

public class DoubleArray extends DoubleSlice {
    
    @SafeVarargs
    public DoubleArray(final double... data) {
        super(data, 0, data.length);
    }
    
    public DoubleArray(final int length) {
        super(new double[length], 0, length);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}