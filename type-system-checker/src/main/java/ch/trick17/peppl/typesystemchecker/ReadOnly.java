package ch.trick17.peppl.typesystemchecker;

import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

import java.lang.annotation.Target;

import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

@TypeQualifier
@SubtypeOf(Inaccessible.class)
@Target({TYPE_USE, TYPE_PARAMETER})
public @interface ReadOnly {}
