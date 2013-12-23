package ch.trick17.peppl.manual.simplegroups;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import ch.trick17.peppl.lib.Mutable;
import ch.trick17.peppl.lib.PepplObject;
import ch.trick17.peppl.lib.Task;
import ch.trick17.peppl.lib.TaskSystem;
import ch.trick17.peppl.lib._Mutable;

public class SimpleGroups implements Callable<Void> {
    
    public static TaskSystem S = TaskSystem.getDefault();
    
    public static void main(final String[] args) {
        new SimpleGroups().call();
    }
    
    @Override
    public Void call() {
        final X x = new X();
        x.y = new Y();
        x.y.i = 10;
        
        x.pass();
        final Task<Void> a = S.run(new A(x));
        
        x.guardRead();
        x.y.guardRead();
        assertEquals(x.y.i, 30);
        
        a.get(); // Propagate exceptions
        return null;
    }
    
    public static class A implements Runnable {
        
        private final @_Mutable X x;
        
        public A(@Mutable final X x) {
            this.x = x;
        }
        
        @Override
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
        
        @Override
        public void run() {
            z.guardRead();
            z.y.guardRead();
            assertEquals(20, z.y.i);
            
            z.releaseShared();
        }
    }
    
    public static class X extends PepplObject {
        public Y y;
    }
    
    public static class Y extends PepplObject {
        public int i;
    }
    
    public static class Z extends PepplObject {
        public Y y;
    }
}
