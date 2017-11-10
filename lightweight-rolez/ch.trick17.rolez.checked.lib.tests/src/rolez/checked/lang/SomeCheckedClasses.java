package rolez.checked.lang;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.Collection;

import rolez.checked.lang.Checked;
import rolez.checked.lang.Checked;

public class SomeCheckedClasses {

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
	
	public static class A extends Checked {
		
		public int value;
		
		public A() {
			this.value = 0;
		}
		
		public int getValue() {
			return this.value;
		}
		
		public void setValue(int value) {
			this.value = value;
		}
	}

	public static class B extends Checked {
		public A a;
		
		public B(A a) {
			this.a = a;
		}
		
        @Override
        protected Iterable<? extends Checked> guardedRefs() {
            return asList(a);
        }
        
        @Override
        protected Collection<? extends Checked> views() {
            return emptyList();
        }
	}
}
