package rolez.lang;

/**
 * Java-side of the Partitioner class, the superclass of all partitioners. Needs to be mapped
 * because the mapped method Slice.partition depends on it and it is impossible to map to methods
 * that use classes generated from Rolez (in the same project).
 * 
 * @author Michael Faes
 */
public abstract class Partitioner {
    public abstract SliceRange[] partition(SliceRange original, int n);
}
