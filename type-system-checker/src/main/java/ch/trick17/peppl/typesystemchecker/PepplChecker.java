package ch.trick17.peppl.typesystemchecker;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.TypeQualifiers;

import ch.trick17.peppl.typesystemchecker.qual.Poly;
import ch.trick17.peppl.typesystemchecker.qual.ReadOnly;
import ch.trick17.peppl.typesystemchecker.qual.ReadWrite;

@TypeQualifiers({ReadOnly.class, ReadWrite.class, Poly.class})
public class PepplChecker extends BaseTypeChecker {
    
    public static final String ILLEGAL_READ = "illegal.read";
    public static final String ILLEGAL_WRITE = "illegal.write";
}
