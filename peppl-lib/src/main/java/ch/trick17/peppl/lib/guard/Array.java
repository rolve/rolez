package ch.trick17.peppl.lib.guard;

public final class Array<E extends Guarded> extends Slice<E> {
    
    @SafeVarargs
    public Array(final E... data) {
        super(data, 0, data.length);
    }
}
