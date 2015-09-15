package ch.trick17.rolez.lib.immutable;

import org.junit.Test;

import ch.trick17.rolez.lib.immutable.ImmutableObject;

@SuppressWarnings("unused")
public class ImmutableObjectTest extends ImmutableObject {
    /* Need to extend ImmutableObject because anonymous classes have a link to
     * this class */
    
    @Test
    public void testCheckClassNoFields() {
        new ImmutableObject() {}.toString();
    }
    
    @Test
    public void testCheckClassPrimitiveField() {
        new ImmutableObject() {
            final int i = 0;
            final double d = 0;
            final char c = 'c';
        }.toString();
    }
    
    @Test
    public void testCheckClassEnumField() {
        new ImmutableObject() {
            final Abc abc = Abc.A;
        }.toString();
    }
    
    private enum Abc {
        A,
        B,
        C;
    }
    
    @Test
    public void testCheckClassKnownImmutableField() {
        new ImmutableObject() {
            final String s = "Hello World!";
        }.toString();
    }
    
    @Test
    public void testCheckClassImmutableField() {
        new ImmutableObject() {
            final SuperImmutable si = null;
        }.toString();
    }
    
    @Test
    public void testCheckClassSuperclass() {
        new SuperImmutable() {
            final long additionalField = 0;
        }.toString();
    }
    
    @Test(expected = AssertionError.class)
    public void testCheckClassNonFinalField() {
        new ImmutableObject() {
            int i = 0;
        }.toString();
    }
    
    @Test(expected = AssertionError.class)
    public void testCheckClassNonImmutableField() {
        new ImmutableObject() {
            final StringBuilder b = new StringBuilder();
        }.toString();
    }
    
    @Test(expected = AssertionError.class)
    public void testCheckClassBadSuperClass() {
        new BadImmutable() {}.toString();
    }
    
    private static class BadImmutable extends ImmutableObject {
        int i = 0;
    }
    
    private static class SuperImmutable extends ImmutableObject {
        final int i = 0;
        final String s = "Bla";
        final SuperImmutable si = null;
    }
}
