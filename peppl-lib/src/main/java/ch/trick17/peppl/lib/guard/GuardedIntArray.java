package ch.trick17.peppl.lib.guard;

import java.util.Collections;

public class GuardedIntArray extends Guarded {
    
    public final int[] data;
    
    @SafeVarargs
    public GuardedIntArray(final int... data) {
        this.data = data;
    }
    
    public GuardedIntSlice slice(final int beginIndex, final int endIndex) {
        return new GuardedIntSlice(data, beginIndex, endIndex);
    }
    
    @Override
    Iterable<? extends Guarded> allRefs() {
        return Collections.emptyList();
    }
}
