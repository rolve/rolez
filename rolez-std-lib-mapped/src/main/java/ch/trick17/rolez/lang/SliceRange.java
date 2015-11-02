package ch.trick17.rolez.lang;

import static java.lang.Math.*;

import java.lang.reflect.Array;

import ch.trick17.rolez.lang.util.Math;

public final class SliceRange {
    
    public static SliceRange forArray(final Object array) {
        assert array.getClass().isArray();
        final int length = Array.getLength(array);
        return new SliceRange(0, length, 1);
    }
    
    public static final SliceRange EMPTY = new SliceRange(0, 0, 1);
    
    public final int begin;
    public final int end;
    public final int step;
    
    public SliceRange(final int begin, final int end, final int step) {
        if(begin < 0 || end < begin || step < 1)
            throw new IllegalArgumentException();
        
        this.begin = begin;
        this.end = end;
        this.step = step;
    }
    
    public int size() {
        final int length = end - begin;
        return (length + step - 1) / step;
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    /**
     * Indicates whether this slice covers the given slice. A slice s1 covers a
     * slice s2, iff the set of indices that s1 represents is a superset of the
     * set of indices s2 represents. In particular, this mean that every slice
     * covers every empty slice, regardless of the beginnings, ends or step
     * sizes of the slices.
     * <p>
     * Note that this contrasts with the semantics of {@link #equals(Object)
     * equals()}, which is defined as the structural equality. This means that
     * two slices that cover each other are not necessarily equal.
     * 
     * @param other
     *            The other slice
     * @return <code>true</code> if this slice cover the given one,
     *         <code>false</code> otherwise.
     */
    public boolean covers(final SliceRange other) {
        final int otherSize = other.size();
        if(otherSize == 0)
            return true;
        
        if(begin > other.begin)
            return false;
        if((begin - other.begin) % step != 0)
            return false;
        if(otherSize == 1)
            /* Only need to cover first element, other's step is irrelevant */
            return other.begin < end;
        if(other.step % step != 0)
            return false;
        if(begin + step * (size() - 1) < other.begin + other.step
                * (otherSize - 1))
            return false;
        return true;
    }
    
    public SliceRange intersectWith(final SliceRange other) {
        if(this.covers(other))
            return other;
        if(other.covers(this))
            return this;
        
        final int minEnd = min(end, other.end);
        if(minEnd <= max(begin, other.begin))
            return EMPTY;
        
        /* A non-empty intersection requires the difference between first begin
         * and second begin to be a multiple of the GCD (greatest common
         * divisor) of the two step sizes. Reason: GCD divides all multiples of
         * either step size and therefore also all differences between
         * multiples. Any such difference between the two begins makes it
         * possible for the two slices to have common elements. */
        final int gcdStep = Math.gcd(step, other.step);
        if(abs(begin - other.begin) % gcdStep != 0)
            return EMPTY;
        
        /* Common indices (possibly) exist, so try to find first */
        int i = begin;
        int j = other.begin;
        while(i != j) {
            if(i < j)
                i += step;
            else
                j += other.step;
            if(max(i, j) >= minEnd)
                return EMPTY;
        }
        
        /* The step size of the intersection is the LCM (least common multiple)
         * of the two step sizes, for rather obvious reasons. */
        final int lcmStep = (step / gcdStep) * other.step;
        return new SliceRange(i, minEnd, lcmStep);
    }
    
    @Override
    public String toString() {
        return "SliceRange[begin=" + begin + ", end=" + end + ", step=" + step
                + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + begin;
        result = prime * result + end;
        result = prime * result + step;
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(!(obj instanceof SliceRange))
            return false;
        final SliceRange other = (SliceRange) obj;
        if(begin != other.begin)
            return false;
        if(end != other.end)
            return false;
        if(step != other.step)
            return false;
        return true;
    }
    
}
