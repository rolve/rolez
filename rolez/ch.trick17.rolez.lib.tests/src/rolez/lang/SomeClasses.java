package rolez.lang;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public abstract class SomeClasses {
    
    public static class Int extends Guarded {
        
        public int value;
        
        public Int() {}
        
        public Int(final int value) {
            this.value = value;
        }
        
        @Override
        protected Iterable<? extends Guarded> guardedRefs() {
            return emptyList();
        }
        
        @Override
        protected Iterable<? extends Guarded> views() {
            return emptyList();
        }
    }
    
    public static class Ref<T extends Guarded> extends Guarded {
        
        public T o;
        
        public Ref() {}
        
        public Ref(final T o) {
            this.o = o;
        }
        
        @Override
        protected Iterable<? extends Guarded> guardedRefs() {
            return asList(o);
        }
        
        @Override
        protected Iterable<? extends Guarded> views() {
            return emptyList();
        }
    }
    
    public static class Node extends Guarded {
        
        public Node next;
        public int data;
        
        public Node() {}
        
        public Node(final int data) {
            this.data = data;
        }
        
        public Node(final Node o) {
            this.next = o;
        }
        
        @Override
        protected Iterable<? extends Guarded> guardedRefs() {
            return asList(next);
        }
        
        @Override
        protected Iterable<? extends Guarded> views() {
            return emptyList();
        }
    }
    
    public static enum SomeEnum {
        A,
        B,
        C;
    }
}
