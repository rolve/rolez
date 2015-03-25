package ch.trick17.peppl.typesystemchecker;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.TypeQualifiers;

import ch.trick17.peppl.typesystemchecker.qual.Inaccessible;
import ch.trick17.peppl.typesystemchecker.qual.ReadOnly;
import ch.trick17.peppl.typesystemchecker.qual.ReadWrite;

@TypeQualifiers({Inaccessible.class, ReadOnly.class, ReadWrite.class})
public class PepplChecker extends BaseTypeChecker {
    
    public static final String ILLEGAL_READ = "illegal.read";
    public static final String ILLEGAL_WRITE = "illegal.write";
    
}
