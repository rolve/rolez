package ch.trick17.peppl.lib;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

public final class Partitioners {
    private Partitioners() {}
    
    public static final Partitioner CONTIGUOUS = new Partitioner() {
        public List<SliceRange> partition(final SliceRange orig, final int n) {
            final int baseSize = orig.size() / n;
            final int largeSlices = orig.size() % n;
            
            final ArrayList<SliceRange> ranges = new ArrayList<>(n);
            int begin = orig.begin;
            for(int i = 0; i < n; i++) {
                final int size =
                        orig.step * (baseSize + (i < largeSlices ? 1 : 0));
                final int end = min(begin + size, orig.end);
                ranges.add(new SliceRange(begin, end, orig.step));
                begin = end;
            }
            assert ranges.size() == n;
            assert begin == orig.end;
            return ranges;
        }
        
        @Override
        public String toString() {
            return "CONTIGUOUS";
        }
    };
    
    public static final Partitioner STRIPED = new Partitioner() {
        public List<SliceRange> partition(final SliceRange orig, final int n) {
            final ArrayList<SliceRange> ranges = new ArrayList<>(n);
            for(int i = 0; i < n; i++)
                ranges.add(new SliceRange(
                        min(orig.begin + i * orig.step, orig.end), orig.end,
                        orig.step * n));
            return ranges;
        }
        
        @Override
        public String toString() {
            return "STRIPED";
        }
    };
}
