package ch.trick17.peppl.manual.simplegroups;

import static org.junit.Assert.assertEquals;
import ch.trick17.peppl.lib.Mutable;
import ch.trick17.peppl.lib._Mutable;
import ch.trick17.peppl.lib.guard.GuardedObject;
import ch.trick17.peppl.lib.task.Task;
import ch.trick17.peppl.lib.task.TaskSystem;

public class SimpleGroups implements Runnable {
    
    public static TaskSystem S = TaskSystem.getDefault();
    
    public static void main(final String[] args) {
        new SimpleGroups().run();
    }
    
    public void run() {
        final X x = new X();
        x.y = new Y();
        x.y.i = 10;
        
        x.pass();
        final Task<Void> a = S.run(new A(x));
        
        x.guardRead();
        x.y.guardRead();
        assertEquals(x.y.i, 30);
        
        a.get(); // Propagate exceptions
    }
    
    public static class A implements Runnable {
        
        private final @_Mutable X x;
        
        public A(@Mutable final X x) {
            this.x = x;
        }
        
        public void run() {
            x.registerNewOwner();
            
            final Y y = x.y;
            
            assertEquals(10, y.i);
            x.y.i += 10;
            
            final Z z = new Z();
            z.y = y;
            
            z.share();
            final Task<Void> b = S.run(new B(z));
            
            y.guardReadWrite();
            y.i += 10;
            
            x.releasePassed();
            b.get();
        }
    }
    
    public static class B implements Runnable {
        
        private final Z z;
        
        public B(final Z z) {
            this.z = z;
        }
        
        public void run() {
            z.guardRead();
            z.y.guardRead();
            assertEquals(20, z.y.i);
            
            z.releaseShared();
        }
    }
    
    public static class X extends GuardedObject {
        public Y y;
    }
    
    public static class Y extends GuardedObject {
        public int i;
    }
    
    public static class Z extends GuardedObject {
        public Y y;
    }
}
