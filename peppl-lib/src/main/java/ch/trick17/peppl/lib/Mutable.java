package ch.trick17.peppl.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Type modifier that indicates that the type is mutable, i.e., it allows write
 * operations. In the real PEPPL, the programmer would use a type modifier to
 * indicate this at method boundaries. Local variable types would be inferred.
 * 
 * @author Michael Faes
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface Mutable {}
