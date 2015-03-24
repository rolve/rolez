package ch.trick17.peppl.typesystemchecker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * To annotate tests that contain type errors.
 * 
 * @author Michael Faes
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeErrors {
    
    /**
     * The line numbers where the type errors occur.
     */
    long[] lines();
}
