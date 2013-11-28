package ch.trick17.peppl.manual.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used by the compiler to indicate that the annotated method is doing a read
 * for which no guard has been inserted (because the method is small and the
 * caller may have more information whether a guard is necessary).
 * <p>
 * This annotation can either be used with parameters or with instance methods,
 * in which case it refers to the target of the method call (the
 * <code>this</code> object).
 * 
 * @author Michael Faes
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.CLASS)
public @interface _UnguardedRead {}
