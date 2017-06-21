package test;

import rolez.annotation.Guarded;
import rolez.annotation.Roleztask;

@Guarded
public class B extends A{

	@Roleztask
	private void task(final boolean $asTask) {
		int i = 1;
	}
}

/*
THIS SHOULD COMPILE INTO THE FOLLOWING:

import static rolez.lang.Guarded.*; 
 
public class B {
	
	public B (final long $task) {
		super();
	}
	
	public void task(final long $task) { }
    
    public rolez.lang.Task<java.lang.Void> $taskTask() {
        return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
            @java.lang.Override
            protected java.lang.Void runRolez() {
                final long $task = idBits();
                return null;
            }
        };
    }
}

*/