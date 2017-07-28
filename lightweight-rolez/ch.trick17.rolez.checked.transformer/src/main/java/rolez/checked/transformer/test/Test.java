package rolez.checked.transformer.test;

import static rolez.checked.lang.Checked.checkLegalRead;
import rolez.annotation.Checked;
import rolez.checked.lang.Task;
import rolez.checked.lang.ThreadPoolTaskSystem;

@Checked
public class Test {
	
	public static void main(String[] args) {

		final ThreadPoolTaskSystem s = new ThreadPoolTaskSystem();
		
		VoidTask vt = new VoidTask(new Runnable() {
			@Override
			public void run() {
				final A a = new A();
		
				Task<?> task = new Task<Void>(new Object[]{a}, new Object[]{}, new Object[]{}) {
		            @Override
		            protected Void runRolez() {
		            	a.foo = 2;
		                return null;
		            }
		        };
		        
		        s.start(task);
				int i = checkLegalRead(a).foo;
				System.out.println(i);
			}
		});
		
		s.run(vt);
	}
	
	private class Foo {
		int i;
		int j;
		
		public Foo(int i, int j) {
			this.i = i;
			this.j = j;
		}
	}
}
