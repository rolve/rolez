package ch.trick17.peppl.lib.immutable;

import org.junit.Test;

@SuppressWarnings("unused")
public class ImmutableTest extends Immutable {
    /* Need to extend Immutable because anonymous classes have a link to this
     * class */
    
    @Test
    public void testCheckClassNoFields() {
        new Immutable() {}.toString();
    }
    
    @Test
    public void testCheckClassPrimitiveField() {
        new Immutable() {
            final int i = 0;
            final double d = 0;
            final char c = 'c';
        }.toString();
    }
    
    @Test
    public void testCheckClassEnumField() {
        new Immutable() {
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
        new Immutable() {
            final String s = "Hello World!";
        }.toString();
    }
    
    @Test
    public void testCheckClassImmutableField() {
        new Immutable() {
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
        new Immutable() {
            int i = 0;
        }.toString();
    }
    
    @Test(expected = AssertionError.class)
    public void testCheckClassNonImmutableField() {
        new Immutable() {
            final StringBuilder b = new StringBuilder();
        }.toString();
    }
    
    @Test(expected = AssertionError.class)
    public void testCheckClassBadSuperClass() {
        new BadImmutable() {}.toString();
    }
    
    private static class BadImmutable extends Immutable {
        int i = 0;
    }
    
    private static class SuperImmutable extends Immutable {
        final int i = 0;
        final String s = "Bla";
        final SuperImmutable si = null;
    }
}
