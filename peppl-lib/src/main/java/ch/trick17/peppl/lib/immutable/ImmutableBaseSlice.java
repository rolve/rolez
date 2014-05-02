package ch.trick17.peppl.lib.immutable;

import java.util.ArrayList;
import java.util.List;

import ch.trick17.peppl.lib.task.Task;

/**
 * Base class for all {@link Immutable} slices, e.g. {@link ImmutableSlice} (for
 * reference types) or {@link ImmutableIntSlice}. A slice represents a
 * contiguous part of an array, e.g. {@link ImmutableArray} (for reference
 * types) or {@link ImmutableIntArray}. Note that an array is just a special
 * case of a slice, namely a slice that covers the whole array.
 * <p>
 * <code>Slice</code> and <code>Array</code> classes in the
 * {@link ch.trick17.peppl.lib.immutable} package are {@link Immutable}, i.e.,
 * they are immutable objects that can be shared with other {@link Task}s. For
 * efficiency reasons, immutable slices expose the underlying array directly as
 * a public field. It is the client's code responsibility to not break the
 * semantics of the class by mutating the array.
 * 
 * @author Michael Faes
 * @param <S>
 *            The concrete slice type.
 */
abstract class ImmutableBaseSlice<S extends ImmutableBaseSlice<S>> extends
        Immutable {
    
    public final int begin;
    public final int end;
    
    public ImmutableBaseSlice(final int beginIndex, final int endIndex) {
        begin = beginIndex;
        end = endIndex;
    }
    
    public final int length() {
        return end - begin;
    }
    
    // IMPROVE: Refactor so that this and BaseSlice use the same code for
    // slicing and partitioning. Could use Java 8 default methods.
    
    public final S slice(final int beginIndex, final int endIndex) {
        assert beginIndex >= begin;
        assert endIndex <= end;
        return createSlice(beginIndex, endIndex);
    }
    
    public final List<S> partition(final int n) {
        final int baseSize = length() / n;
        final int largeSlices = length() % n;
        
        final ArrayList<S> slices = new ArrayList<>(n);
        int beginIndex = begin;
        for(int i = 0; i < n; i++) {
            final int endIndex = beginIndex + baseSize
                    + (i < largeSlices ? 1 : 0);
            slices.add(slice(beginIndex, endIndex));
            beginIndex = endIndex;
        }
        assert slices.size() == n;
        assert beginIndex == end;
        return slices;
    }
    
    abstract S createSlice(int beginIndex, int endIndex);
}
