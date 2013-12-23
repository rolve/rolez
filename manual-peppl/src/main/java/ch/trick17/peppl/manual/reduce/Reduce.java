package ch.trick17.peppl.manual.reduce;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.concurrent.Callable;

import ch.trick17.peppl.lib.task.Task;
import ch.trick17.peppl.lib.task.TaskSystem;

public class Reduce implements Callable<Void> {
    
    private static final int SIZE = 1000;
    private static int SPLIT_SIZE = SIZE / 2;
    
    public static void main(final String[] args) {
        TaskSystem.getDefault().run(new Reduce()).get();
    }
    
    @Override
    public Void call() {
        final int[] data = shuffledInts();
        
        final Task<Long> task = TaskSystem.getDefault().run(
                new SumPrimesTask(data, 0, SIZE));
        
        final long sum = task.get();
        assertEquals(76125, sum);
        return null;
    }
    
    public class SumPrimesTask implements Callable<Long> {
        
        private final int[] data;
        private final int begin;
        private final int end;
        
        public SumPrimesTask(final int[] data, final int begin, final int end) {
            this.data = data;
            this.begin = begin;
            this.end = end;
        }
        
        @Override
        public Long call() throws Exception {
            final int size = end - begin;
            if(size <= SPLIT_SIZE) {
                long sum = 0;
                for(int i = begin; i < end; i++)
                    // No guard necessary, data is guaranteed to be available
                    if(isPrime(data[i]))
                        sum += data[i];
                return sum;
            }
            else {
                final int cut = begin + size / 2;
                final Task<Long> leftTask = TaskSystem.getDefault().run(
                        new SumPrimesTask(data, begin, cut));
                final Task<Long> rightTask = TaskSystem.getDefault().run(
                        new SumPrimesTask(data, cut, end));
                return leftTask.get() + rightTask.get();
            }
        }
    }
    
    private static int[] shuffledInts() {
        final int[] data = new int[SIZE];
        final Random random = new Random();
        for(int i = 0; i < SIZE; i++)
            data[i] = i + 2;
        for(int i = SIZE - 1; i > 0; i--) {
            final int index = random.nextInt(i + 1);
            
            final int a = data[index];
            data[index] = data[i];
            data[i] = a;
        }
        return data;
    }
    
    private static boolean isPrime(final int n) {
        for(int i = 2; i < Math.sqrt(n) + 1; i++)
            if(n % i == 0)
                return false;
        return true;
    }
}
