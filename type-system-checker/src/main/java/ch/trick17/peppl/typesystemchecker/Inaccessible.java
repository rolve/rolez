package ch.trick17.peppl.typesystemchecker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

/**
 * The top type in the PEPPL type system.
 * 
 * @author Michael Faes
 */
@TypeQualifier
@SubtypeOf({})
@DefaultQualifierInHierarchy
@Target({ElementType.TYPE_USE})
public @interface Inaccessible {}
