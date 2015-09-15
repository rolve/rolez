package ch.trick17.rolez.lib;

import java.util.List;

public interface Partitioner {
    
    List<SliceRange> partition(SliceRange original, int n);
}
