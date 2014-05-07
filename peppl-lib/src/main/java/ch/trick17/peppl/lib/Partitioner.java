package ch.trick17.peppl.lib;

import java.util.List;

public interface Partitioner {
    
    List<SliceRange> partition(SliceRange original, int n);
}
