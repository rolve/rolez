package test;

import rolez.annotation.Checked;
import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.annotation.Roleztask;
import rolez.checked.lang.Task;

@Checked
public class Test {

	A field1;
	B field2;
	
	public static void main(String[] args) {
		int i = 1;
    	i++;
    	new A(i);
	}
	
	public Task<Void> $mainTask1() {
        return new Task<Void>(new Object[]{this}, new Object[]{}, new Object[]{}) {
            @Override
            protected java.lang.Void runRolez() {
            	int i = 1;
            	i++;
            	new A(i);
                return null;
            }
        };
    }

	public void testOriginalNotRolez(A src, A dst, final boolean $asTask) {
		dst.foo = foo(true);
		int i = src.foo;
		dst.foo = dst.foo + i;
	}
	
	@Roleztask
	public void testOriginal(@Readonly A src, @Readwrite A dst, final boolean $asTask) {
		dst.foo = foo(true);
		int i = src.foo;
		dst.foo = dst.foo + i;
	}
	
	public Task<Void> testOriginalTask(final A src, final A dst, boolean $asTask) {
		
		return new Task<Void>(new Object[]{dst}, new Object[]{src}, new Object[]{}) {
	        @Override
	        protected Void runRolez() {
	        	dst.foo = foo(true);
	    		int i = src.foo;
	    		dst.foo = dst.foo + i;
	    		return null;
	        }
	    };
	}
	
	@Roleztask
	public int foo(final boolean $asTask) {
		int i = 42;
		return i;
	}
}
