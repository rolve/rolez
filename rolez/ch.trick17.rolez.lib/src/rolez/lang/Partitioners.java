package rolez.lang;

import static java.lang.Math.min;

public final class Partitioners {
    private Partitioners() {}
    
    public static final Partitioner CONTIGUOUS = new Partitioner() {
        public GuardedArray<SliceRange[]> partition(final SliceRange orig, final int n) {
            final int baseSize = orig.size() / n;
            final int largeSlices = orig.size() % n;
            
            final SliceRange[] ranges = new SliceRange[n];
            int begin = orig.begin;
            for(int i = 0; i < n; i++) {
                final int size = orig.step * (baseSize + (i < largeSlices ? 1 : 0));
                final int end = min(begin + size, orig.end);
                ranges[i] = new SliceRange(begin, end, orig.step);
                begin = end;
            }
            return new GuardedArray<>(ranges);
        }
        
        @Override
        public String toString() {
            return "CONTIGUOUS";
        }
    };
    
    public static final Partitioner STRIPED = new Partitioner() {
        public GuardedArray<SliceRange[]> partition(final SliceRange orig, final int n) {
            final SliceRange[] ranges = new SliceRange[n];
            for(int i = 0; i < n; i++)
                ranges[i] = new SliceRange(min(orig.begin + i * orig.step, orig.end), orig.end,
                        orig.step * n);
                        
            return new GuardedArray<>(ranges);
        }
        
        @Override
        public String toString() {
            return "STRIPED";
        }
    };
}
