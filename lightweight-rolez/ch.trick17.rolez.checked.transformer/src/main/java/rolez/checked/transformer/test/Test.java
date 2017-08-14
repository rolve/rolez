package rolez.checked.transformer.test;

import rolez.annotation.Checked;
import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.annotation.Roleztask;
import rolez.checked.lang.Task;
import rolez.checked.lang.TaskSystem;

@Checked
public class Test {

	A field1;
	B field2;
	
	public static void main(String[] args) {
		
	}

	@Roleztask
	public void testOriginal(@Readonly A src, @Readwrite A dst, final boolean $asTask) {
		foo(true);
	}
	
	@Roleztask
	public void foo(final boolean $asTask) {
		int i = 0;
		i++;
	}
}
