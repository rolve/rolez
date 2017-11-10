package rolez.checked.lang;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.Collection;

import rolez.checked.lang.Checked;

public abstract class SomeClasses {
    
    public static class Int extends Checked {
        
        public int value;
        
        public Int() {}
        
        public Int(final int value) {
            this.value = value;
        }
        
        @Override
        protected Iterable<? extends Checked> guardedRefs() {
            return emptyList();
        }
        
        @Override
        protected Collection<? extends Checked> views() {
            return emptyList();
        }
    }
    
    public static class Ref<T extends Checked> extends Checked {
        
        public T o;
        
        public Ref() {}
        
        public Ref(final T o) {
            this.o = o;
        }
        
        @Override
        protected Iterable<? extends Checked> guardedRefs() {
            return asList(o);
        }
        
        @Override
        protected Collection<? extends Checked> views() {
            return emptyList();
        }
    }
    
    public static class Node extends Checked {
        
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
        protected Iterable<? extends Checked> guardedRefs() {
            return asList(next);
        }
        
        @Override
        protected Collection<? extends Checked> views() {
            return emptyList();
        }
    }
    
    public static enum SomeEnum {
        A,
        B,
        C;
    }
}
