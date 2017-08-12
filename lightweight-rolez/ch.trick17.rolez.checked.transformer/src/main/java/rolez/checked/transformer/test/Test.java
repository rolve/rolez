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
	public void testOriginal(@Readonly A src, @Readwrite A dst, final boolean $asTask) {
		dst.foo = src.foo;
	}
	
	// How the original method should look like after transformation
	public void testIf(@Readonly A src, @Readwrite A dst, final boolean $asTask) {
		if ($asTask) {
			$testTask(src, dst);
		} else {
			dst.foo = src.foo;
		}
	}
	
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
