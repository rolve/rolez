package ch.trick17.rolez.lang;

import java.util.List;

public interface Partitioner {
    
    List<SliceRange> partition(SliceRange original, int n);
}
