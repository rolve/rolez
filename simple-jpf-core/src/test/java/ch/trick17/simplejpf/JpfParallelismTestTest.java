package ch.trick17.simplejpf;

import org.junit.Test;

import ch.trick17.simplejpf.test.JpfParallelismTest;

public class JpfParallelismTestTest extends JpfParallelismTest {
    
    @Test
    public void testVerifyParallelism() {
        if(verifyParallelism(new int[][]{{0, 1}})) {
            new Thread(newRegion(0)).start();
            region(1);
        }
    }
    
    @Test
    public void testVerifyParallelismJoin() throws InterruptedException {
        if(verifyParallelism(new int[][]{{0, 1}, {1, 2}})) {
            final Thread first = new Thread(newRegion(0));
            first.start();
            
            new Thread(newRegion(1)).start();
            
            first.join();
            region(2);
        }
    }
    
    @Test(expected = AssertionError.class)
    public void testVerifyParallelismFailSequential() {
        if(verifyParallelism(new int[][]{{0, 1}})) {
            region(0);
            region(1);
        }
    }
    
    @Test(expected = AssertionError.class)
    public void testVerifyParallelismFailJoin() throws InterruptedException {
        if(verifyParallelism(new int[][]{{0, 2}})) {
            final Thread first = new Thread(newRegion(0));
            first.start();
            
            first.join();
            region(2);
        }
    }
    
    @Test
    public void testVerifyParallelismMultiple() {
        if(verifyParallelism(new int[][]{{0, 1, 2, 3}})) {
            for(int i = 0; i < 3; i++)
                new Thread(newRegion(i)).start();
            
            region(3);
        }
    }
    
    @Test
    public void testVerifyParallelismMultiMultiple()
            throws InterruptedException {
        if(verifyParallelism(new int[][]{{0, 1}, {2, 3}})) {
            final Thread[] threads = new Thread[2];
            for(int i = 0; i < 2; i++)
                threads[i] = new Thread(newRegion(i));
            for(final Thread thread : threads)
                thread.start();
            
            for(final Thread thread : threads)
                thread.join();
            
            for(int i = 0; i < 2; i++)
                new Thread(newRegion(i + 2)).start();
        }
    }
    
    @Test
    public void testVerifyParallelismNoRegions() {
        if(verifyParallelism(new int[][]{})) {
            /* Nothing at all */
        }
    }
    
    private Runnable newRegion(final int id) {
        return new Runnable() {
            @Override
            public void run() {
                region(id);
            }
        };
    }
}
