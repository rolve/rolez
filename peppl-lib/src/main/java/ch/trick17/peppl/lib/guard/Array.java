package ch.trick17.peppl.lib.guard;

import java.util.Arrays;

public final class Array<E extends Guarded> extends Slice<E> {
    
    @SafeVarargs
    public Array(final E... data) {
        super(data, 0, data.length);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}
