package rolez.lang;

// TODO: Convert to Rolez (would profit from interfaces or at least abstract classes)

public interface Partitioner {
    
    // TODO: Replace return type with some final or even immutable array class
    GuardedArray<SliceRange[]> partition(SliceRange original, int n);
}
