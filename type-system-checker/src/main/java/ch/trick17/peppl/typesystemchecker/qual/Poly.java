package ch.trick17.peppl.typesystemchecker.qual;

import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

import java.lang.annotation.Target;

import org.checkerframework.framework.qual.PolymorphicQualifier;
import org.checkerframework.framework.qual.TypeQualifier;

@TypeQualifier
@PolymorphicQualifier
@Target({TYPE_USE, TYPE_PARAMETER})
public @interface Poly {}
