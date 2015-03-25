package ch.trick17.peppl.typesystemchecker.qual;

import static com.sun.source.tree.Tree.Kind.*;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

import java.lang.annotation.Target;

import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

@TypeQualifier
@SubtypeOf(ReadOnly.class)
@DefaultQualifierInHierarchy
@ImplicitFor(trees = {NEW_CLASS, NEW_ARRAY, PLUS, NULL_LITERAL})
@Target({TYPE_USE, TYPE_PARAMETER})
public @interface ReadWrite {}
