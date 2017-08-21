package test;

import rolez.annotation.Checked;
import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.annotation.Roleztask;

@Checked
public class Test {

	A field1;
	B field2;
	
	public static void main(String[] args) {
		int i = 1;
    	i++;
    	new A(i);
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
	
	@Roleztask
	public int foo(final boolean $asTask) {
		int i = 42;
		return i;
	}
}
