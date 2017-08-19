package ch.trick17.rolez.generator;

/**
 * Represents the different kinds of methods that the generator
 * produces based on a single Rolez method.
 */
public enum MethodKind {

    GUARDED_METHOD(""), UNGUARDED_METHOD("$Unguarded"), TASK("$Task");
    
    public final String suffix;

    MethodKind(String suffix) {
        this.suffix = suffix;
    }
}
