package ch.trick17.peppl.lib.guard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import ch.trick17.peppl.lib.SomeClasses.SomeEnum;
import ch.trick17.peppl.lib.SomeClasses.SomeImmutable;

public class GuardedObjectTest extends GuardedObject {
    
    @Test
    public void testGuardedRefsGuardedObject() {
        final GuardedObject object = new GuardedObject();
        assertEquals(0, count(object.guardedRefs()));
    }
    
    @Test
    public void testGuardedRefsGuardedFields() {
        final GuardedObject object = new GuardedFields();
        assertTrue(count(object.guardedRefs()) == 2);
    }
    
    static class GuardedFields extends GuardedObject {
        GuardedObject o = new GuardedObject();
        Array<Guarded> a = new Array<Guarded>();
    }
    
    @Test
    public void testGuardedRefsPrimitiveFields() {
        final GuardedObject object = new PrimitiveFields();
        assertEquals(0, count(object.guardedRefs()));
    }
    
    static class PrimitiveFields extends GuardedObject {
        int i;
        double d;
        boolean b;
    }
    
    @Test
    public void testGuardedRefsAnonymousClass() {
        final GuardedObject object = new GuardedObject() {};
        // One reference to the wrapping GuardedObjectTest
        assertEquals(1, count(object.guardedRefs()));
    }
    
    @Test
    public void testGuardedRefsImmutableReferenceFields() {
        final GuardedObject object = new ImmutableReferenceFields();
        assertTrue(count(object.guardedRefs()) == 0);
    }
    
    static class ImmutableReferenceFields extends GuardedObject {
        SomeEnum abc = SomeEnum.A;
        String s = "I'm so immutable...";
        SomeImmutable i = new SomeImmutable();
    }
    
    private static int count(final Iterable<?> iterable) {
        if(iterable instanceof Collection<?>)
            return ((Collection<?>) iterable).size();
        
        int count = 0;
        for(final Iterator<?> i = iterable.iterator(); i.hasNext(); i.next())
            count++;
        return count;
    }
}
