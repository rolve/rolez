package rolez.lang;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.Collection;

public class SomeCheckedClasses {

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
        protected Iterable<? extends Guarded> guardedRefs() {
            return asList(a);
        }
        
        @Override
        protected Collection<? extends Guarded> views() {
            return emptyList();
        }
	}
}
