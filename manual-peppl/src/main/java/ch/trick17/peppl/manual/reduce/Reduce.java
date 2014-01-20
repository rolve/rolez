package ch.trick17.peppl.manual.reduce;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.concurrent.Callable;

import ch.trick17.peppl.lib.guard.IntArray;
import ch.trick17.peppl.lib.task.Task;
import ch.trick17.peppl.lib.task.TaskSystem;

public class Reduce implements Callable<Void> {
    
    private static final int SIZE = 1000;
    private static int SPLIT_SIZE = SIZE / 2;
    
    public static void main(final String[] args) {
        TaskSystem.getDefault().runDirectly(new Reduce());
    }
    
    public Void call() {
        final IntArray array = shuffledInts();
        
        array.share();
        final Task<Long> task = TaskSystem.getDefault().run(
                new SumPrimesTask(array, 0, SIZE));
        
        final long sum = task.get();
        assertEquals(76125, sum);
        return null;
    }
    
    public class SumPrimesTask implements Callable<Long> {
        
        private final IntArray array;
        private final int begin;
        private final int end;
        
        public SumPrimesTask(final IntArray array, final int begin,
                final int end) {
            this.array = array;
            this.begin = begin;
            this.end = end;
        }
        
        public Long call() {
            final int size = end - begin;
            if(size <= SPLIT_SIZE) {
                long sum = 0;
                array.guardRead(); // Do check before loop
                for(int i = begin; i < end; i++)
                    if(isPrime(array.data[i]))
                        sum += array.data[i];
                array.releaseShared();
                return sum;
            }
            else {
                final int cut = begin + size / 2;
                array.share();
                final Task<Long> leftTask = TaskSystem.getDefault().run(
                        new SumPrimesTask(array, begin, cut));
                array.share();
                final Task<Long> rightTask = TaskSystem.getDefault().run(
                        new SumPrimesTask(array, cut, end));
                array.releaseShared();
                return leftTask.get() + rightTask.get();
            }
        }
    }
    
    private static IntArray shuffledInts() {
        final IntArray array = new IntArray(new int[SIZE]);
        final Random random = new Random();
        for(int i = 0; i < SIZE; i++)
            array.data[i] = i;
        for(int i = SIZE - 1; i > 0; i--) {
            final int index = random.nextInt(i + 1);
            
            final int t = array.data[index];
            array.data[index] = array.data[i];
            array.data[i] = t;
        }
        return array;
    }
    
    private static boolean isPrime(final int n) {
        if(n < 2)
            return false;
        for(int i = 2; i < Math.sqrt(n) + 1; i++)
            if(n % i == 0)
                return false;
        return true;
    }
}
