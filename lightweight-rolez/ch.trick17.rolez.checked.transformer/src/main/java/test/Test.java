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
		Test test = new Test();
		test.testOriginal(new A(), new A(), true);
	}

	@Roleztask
	public void testOriginal(@Readonly A src, @Readwrite A dst, final boolean $asTask) {
		dst.foo = foo(true);
		System.out.println(src.foo);
		System.out.println(dst.foo);
	}
	
	@Roleztask
	public int foo(final boolean $asTask) {
		int i = 42;
		return i;
	}
}
