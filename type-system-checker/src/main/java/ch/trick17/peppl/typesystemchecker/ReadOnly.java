package ch.trick17.peppl.typesystemchecker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

@TypeQualifier
@SubtypeOf(Inaccessible.class)
@Target(ElementType.TYPE_USE)
public @interface ReadOnly {}
