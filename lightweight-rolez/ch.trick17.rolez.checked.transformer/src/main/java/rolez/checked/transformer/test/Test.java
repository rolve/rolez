package rolez.checked.transformer.test;

import rolez.annotation.Checked;
import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.annotation.Roleztask;
import rolez.checked.lang.Task;

@Checked
public class Test {
	
	public static void main(String[] args) {
		
	}
	

	// Original code
	@Roleztask
	public int testOriginal(@Readonly A src, @Readwrite A dst, final boolean $asTask) {
		dst.foo = src.foo;
		return dst.foo;
	}

	/*
	// Resulting methods after transformation
	public void test(final A src, final A dst, final boolean $asTask) {
		if ($asTask) {
			Task<Void> task = $testTask(src, dst);
			task.run();
		} else {
			$test(src, dst);
		}
	}
	
	// Method for sequential execution -> essentially the same as the method before
	public void $test(final A src, final A dst) {
		dst.foo = src.foo;
	}
	*/
	
	// Method which returns the rolez task
	public Task<Void> $testTask(final A src, final A dst) {
        return new Task<Void>(new Object[]{dst}, new Object[]{src}, new Object[]{}) {
            @Override
            protected Void runRolez() {
            	dst.foo = src.foo;
                return null;
            }
        };
    }
}
