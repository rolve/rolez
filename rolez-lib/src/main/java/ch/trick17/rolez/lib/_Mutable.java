package ch.trick17.rolez.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used by the compiler to annotate task fields as mutable. This is a separate
 * annotation to restrict the use of the programmer-used {@link Mutable}
 * annotations to local variables, parameters and methods.
 * 
 * @author Michael Faes
 */
@Target(ElementType.FIELD)
public @interface _Mutable {}
