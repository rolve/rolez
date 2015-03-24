package ch.trick17.peppl.typesystemchecker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

import com.sun.source.tree.Tree;

@TypeQualifier
@SubtypeOf(ReadOnly.class)
@Target(ElementType.TYPE_USE)
@ImplicitFor(trees = {Tree.Kind.NEW_CLASS, Tree.Kind.NULL_LITERAL})
public @interface ReadWrite {}
