package ch.trick17.peppl.typesystemchecker;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.TypeQualifiers;

@TypeQualifiers({Inaccessible.class, ReadOnly.class, ReadWrite.class})
public class PepplChecker extends BaseTypeChecker {}
