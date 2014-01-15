package ch.trick17.peppl.lib.guard;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GuardedArray<E extends GuardedObject> extends Guarded {
    
    public final E[] data;
    
    @SafeVarargs
    public GuardedArray(final E... data) {
        this.data = data;
    }
    
    @Override
    List<?> allRefs() {
        return Collections.unmodifiableList(Arrays.asList(data));
    }
}
