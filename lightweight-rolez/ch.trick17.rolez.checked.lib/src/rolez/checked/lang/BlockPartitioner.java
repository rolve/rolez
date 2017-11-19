package rolez.checked.lang;

public class BlockPartitioner extends rolez.checked.lang.Partitioner {
    
    public final int blockSize;
    
    public BlockPartitioner(final int blockSize, final long $task) {
        super();
        new rolez.checked.lang.Assertion(blockSize > 0, "block size must be positive");
        this.blockSize = blockSize;
    }
    
    public rolez.checked.lang.SliceRange[] partition(final rolez.checked.lang.SliceRange orig, final int n, final long $task) {
        new rolez.checked.lang.Assertion((orig.size() % this.blockSize) == 0, "size not a multiple of " + this.blockSize);
        final int blocks = orig.size() / this.blockSize;
        final int baseSize = (blocks / n) * this.blockSize;
        final int largeRanges = blocks % n;
        final rolez.checked.lang.CheckedVectorBuilder<rolez.checked.lang.SliceRange[]> ranges = new rolez.checked.lang.CheckedVectorBuilder<rolez.checked.lang.SliceRange[]>(new rolez.checked.lang.SliceRange[n]);
        int begin = orig.begin;
        for(int i = 0; i < n; i++) {
            int size = orig.step * baseSize;
            if(i < largeRanges)
                size += orig.step * this.blockSize;
            
            final int end = java.lang.Math.min(begin + size, orig.end);
            ranges.set(i, new rolez.checked.lang.SliceRange(begin, end, orig.step));
            begin = end;
        }
        return ranges.build().getData();
    }
    
    @java.lang.Override
    public rolez.checked.lang.SliceRange[] partition(final rolez.checked.lang.SliceRange orig, final int n) {
        return this.partition(orig, n, rolez.checked.lang.Task.currentTask().idBits());
    }
    
    public java.lang.String toString(final long $task) {
        return this.blockSize + "-BlockPartitioner";
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return this.toString(rolez.checked.lang.Task.currentTask().idBits());
    }
}
