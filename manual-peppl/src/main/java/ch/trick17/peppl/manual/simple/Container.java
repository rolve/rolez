package ch.trick17.peppl.manual.simple;

import ch.trick17.peppl.manual.lib.Mutable;
import ch.trick17.peppl.manual.lib._UnguardedRead;
import ch.trick17.peppl.manual.lib._UnguardedReadWrite;

public class Container {
    
    private int i;
    
    @_UnguardedRead
    public int get() {
        return i;
    }
    
    @Mutable
    @_UnguardedReadWrite
    public void set(final int c) {
        i = c;
    }
}
