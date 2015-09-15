package ch.trick17.rolez.lib.immutable;

import org.junit.Test;

import ch.trick17.rolez.lib.SomeClasses.SomeEnum;
import ch.trick17.rolez.lib.SomeClasses.SomeImmutable;
import ch.trick17.rolez.lib.guard.GuardedObject;
import ch.trick17.rolez.lib.immutable.ImmutableArray;
import ch.trick17.rolez.lib.immutable.ImmutableSlice;

public class ImmutableSliceTest {
    
    @Test
    public void testConstructorImmutableClass() {
        // Using arrays for their simpler constructor
        new ImmutableArray<>(new SomeImmutable[10]).toString();
        new ImmutableArray<>(new ImmutableSlice[10]).toString();
    }
    
    @Test
    public void testConstructorEnum() {
        new ImmutableArray<>(new SomeEnum[10]).toString();
    }
    
    @Test
    public void testConstructorKnownImmutable() {
        new ImmutableArray<>(new String[10]).toString();
    }
    
    @Test(expected = AssertionError.class)
    public void testConstructorMutable() {
        new ImmutableArray<>(new StringBuilder[10]).toString();
    }
    
    @Test(expected = AssertionError.class)
    public void testConstructorGuarded() {
        new ImmutableArray<>(new GuardedObject[10]).toString();
    }
}
