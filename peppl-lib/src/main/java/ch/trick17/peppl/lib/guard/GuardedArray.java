package ch.trick17.peppl.lib.guard;

public final class GuardedArray<E extends Guarded> extends GuardedSlice<E> {
    
    @SafeVarargs
    public GuardedArray(final E... data) {
        super(data, 0, data.length);
    }
}
