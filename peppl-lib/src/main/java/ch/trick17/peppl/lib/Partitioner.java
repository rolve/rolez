package ch.trick17.peppl.lib;

import java.util.List;

public interface Partitioner {
    
    List<SliceDef> partition(SliceDef original, int n);
    
    final class SliceDef {
        public final int begin;
        public final int end;
        public final int step;
        
        public SliceDef(final int begin, final int end, final int step) {
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
            return "SliceDef[begin=" + begin + ", end=" + end + ", step="
                    + step + "]";
        }
    }
}
