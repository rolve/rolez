package test;

import java.util.Random;

import rolez.annotation.Checked;
import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.annotation.Roleztask;
import rolez.checked.internal.Tasks;
import rolez.checked.lang.Task;
import rolez.checked.lang.TaskSystem;

@Checked
public class Test {

	public static void main(String[] args) {
		
	}
	
	public void test() {
		Random r = new Random();
		boolean b = r.nextBoolean(); // Could be everything we don't know at compile/transform-time
		int i = bar(b);
		int j = i;
	}

	@Roleztask
	public int bar(final boolean $asTask) {
		int i = 1;
		return i;
	}

	public void testTransformed() {
		Task<Integer> t = null;
		int i = 0;
		
		Random r = new Random();
		boolean b = r.nextBoolean();
		
		if (b) {
			t = (Task<Integer>) barTransformed(true);
		} else {
			i = (Integer) barTransformed(false);
		}
		
		if (b) {
			i = t.get();
		}
		int j = i;
	}

	public Object barTransformed(final boolean $asTask) {
		if (!$asTask) {
			int i = 1;
			return i;
		} 
		
		final Tasks $tasks = new Tasks();
		try {
			return $tasks.addInline(TaskSystem.getDefault().start(this.$barTask()));
		}
		finally {
			$tasks.joinAll();
		}
	}
	
	public Task<Integer> $barTask() {
        return new Task<Integer>(new Object[]{}, new Object[]{}, new Object[]{}) {
            @java.lang.Override
            protected Integer runRolez() {
    			int i = 1;
                return i;
            }
        };
    }
	
	@Roleztask
	public void foo(@Readonly A src, @Readwrite A dst, final boolean $asTask) {
		dst.foo = foo1(true);
		int i = src.foo;
		dst.foo = dst.foo + i;
	}
	
	@Roleztask
	public int foo1(final boolean $asTask) {
		int i = 42;
		return i;
	}
}
