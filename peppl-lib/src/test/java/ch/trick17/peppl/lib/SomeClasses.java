package ch.trick17.peppl.lib;

import ch.trick17.peppl.lib.guard.Guarded;
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
    
    public static class Ref<T extends Guarded> extends GuardedObject {
        
        public T o;
        
        public Ref() {}
        
        public Ref(final T o) {
            this.o = o;
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
