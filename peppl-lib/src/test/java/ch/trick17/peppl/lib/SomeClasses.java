package ch.trick17.peppl.lib;

import ch.trick17.peppl.lib.guard.GuardedObject;
import ch.trick17.peppl.lib.immutable.ImmutableObject;

public abstract class SomeClasses {
    
    public static class Int extends GuardedObject {
        
        public int value;
        
        public Int() {}
        
        public Int(final int value) {
            this.value = value;
        }
    }
    
    public static class IntContainer extends GuardedObject {
        
        public Int i;
        
        public IntContainer() {
            i = new Int();
        }
        
        public IntContainer(final Int i) {
            this.i = i;
        }
    }
    
    public static class Node extends GuardedObject {
        
        public Node next;
        public int data;
        
        public Node() {}
        
        public Node(final int data) {
            this.data = data;
        }
        
        public Node(final Node o) {
            this.next = o;
        }
    }
    
    public static class SomeImmutable extends ImmutableObject {}
    
    public static enum SomeEnum {
        A,
        B,
        C;
    }
}
