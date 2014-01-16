package ch.trick17.peppl.lib.guard;

import java.util.Arrays;
import java.util.Collections;

public class GuardedArray<E extends Guarded> extends Guarded {
    
    public final E[] data;
    
    @SafeVarargs
    public GuardedArray(final E... data) {
        this.data = data;
    }
    
    public GuardedSlice<E> slice(final int beginIndex, final int endIndex) {
        return new GuardedSlice<E>(data, beginIndex, endIndex);
    }
    
    @Override
    Iterable<? extends Guarded> allRefs() {
        return Collections.unmodifiableList(Arrays.asList(data));
    }
}
