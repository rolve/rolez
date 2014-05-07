package ch.trick17.peppl.lib;

import java.lang.reflect.Array;

public final class SliceRange {
    
    public static SliceRange forArray(final Object array) {
        assert array.getClass().isArray();
        final int length = Array.getLength(array);
        return new SliceRange(0, length, 1);
    }
    
    public final int begin;
    public final int end;
    public final int step;
    
    public SliceRange(final int begin, final int end, final int step) {
        assert begin >= 0;
        assert end >= begin;
        assert step > 0;
        
        this.begin = begin;
        this.end = end;
        this.step = step;
    }
    
    public int size() {
        final int length = end - begin;
        return (length - 1) / step + 1;
    }
    
    @Override
    public String toString() {
        return "SliceDef[begin=" + begin + ", end=" + end + ", step=" + step
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
