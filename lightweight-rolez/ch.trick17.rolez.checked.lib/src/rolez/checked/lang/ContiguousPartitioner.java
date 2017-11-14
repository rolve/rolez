package rolez.checked.lang;


public final class ContiguousPartitioner extends Partitioner {
    
    public static final ContiguousPartitioner INSTANCE = new ContiguousPartitioner();
    
    private ContiguousPartitioner() {}
    
    public SliceRange[] partition(final SliceRange orig, final int n, final long $task) {
        final int baseSize = orig.size() / n;
        final int largeRanges = orig.size() % n;
        final GuardedVectorBuilder<SliceRange[]> ranges = new GuardedVectorBuilder<SliceRange[]>(new SliceRange[n]);
        int begin = orig.begin;
        for(int i = 0; i < n; i++) {
            int size = orig.step * baseSize;
            if(i < largeRanges)
                size += orig.step;
            
            final int end = java.lang.Math.min(begin + size, orig.end);
            ranges.set(i, new SliceRange(begin, end, orig.step));
            begin = end;
        }
        return ranges.build().getData();
    }
    
    @java.lang.Override
    public SliceRange[] partition(final SliceRange orig, final int n) {
        return this.partition(orig, n, Task.currentTask().idBits());
    }
    
    public java.lang.String toString(final long $task) {
        return "ContiguousPartitioner";
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return this.toString(Task.currentTask().idBits());
    }
}
