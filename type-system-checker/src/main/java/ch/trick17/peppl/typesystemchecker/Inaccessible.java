package ch.trick17.peppl.typesystemchecker;

import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static org.checkerframework.framework.qual.DefaultLocation.IMPLICIT_UPPER_BOUNDS;
import static org.checkerframework.framework.qual.DefaultLocation.LOCAL_VARIABLE;
import static org.checkerframework.framework.qual.DefaultLocation.RESOURCE_VARIABLE;

import java.lang.annotation.Target;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

/**
 * The top type in the PEPPL type system.
 * 
 * @author Michael Faes
 */
@TypeQualifier
@SubtypeOf({})
@DefaultFor({LOCAL_VARIABLE, RESOURCE_VARIABLE, IMPLICIT_UPPER_BOUNDS})
@Target({TYPE_USE, TYPE_PARAMETER})
public @interface Inaccessible {}
