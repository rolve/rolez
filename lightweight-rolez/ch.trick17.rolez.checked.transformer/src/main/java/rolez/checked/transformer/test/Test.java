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
	
	// Resulting methods after transformation
	public Integer test(final A src, final A dst, final boolean $asTask) {
		if ($asTask) {
			Task<Integer> task = $testTask(src, dst);
			task.run();
			return task.get();
		} else {
			return $test(src, dst);
		}
	}
	
	public int $test(final A src, final A dst) {
		dst.foo = src.foo;
		return dst.foo;
	}
	
	public Task<Integer> $testTask(final A src, final A dst) {
        return new Task<Integer>(new Object[]{dst}, new Object[]{src}, new Object[]{}) {
            @Override
            protected Integer runRolez() {
            	dst.foo = src.foo;
                return dst.foo;
            }
        };
    }
}
