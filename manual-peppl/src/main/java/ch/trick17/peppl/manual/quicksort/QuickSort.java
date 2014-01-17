package ch.trick17.peppl.manual.quicksort;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import ch.trick17.peppl.lib._UnguardedRead;
import ch.trick17.peppl.lib.guard.IntArray;
import ch.trick17.peppl.lib.guard.IntSlice;
import ch.trick17.peppl.lib.task.NewThreadTaskSystem;
import ch.trick17.peppl.lib.task.Task;
import ch.trick17.peppl.lib.task.TaskSystem;

public class QuickSort implements Runnable {
    
    private static final TaskSystem SYSTEM = new NewThreadTaskSystem();
    
    public static void main(final String[] args) {
        new QuickSort(50000).run();
    }
    
    private final int size;
    
    public QuickSort(final int size) {
        this.size = size;
    }
    
    public void run() {
        final IntArray array = shuffledInts();
        
        final long start = System.nanoTime();
        
        array.pass();
        final Task<Void> task = SYSTEM.run(new SortTask(array));
        array.guardRead();
        
        System.out.println((System.nanoTime() - start) / 1000000000.0);
        
        for(int i = 0; i < size; i++)
            assertEquals(i, array.data[i]);
        task.get();
    }
    
    public class SortTask implements Runnable {
        
        private final IntSlice s;
        
        public SortTask(final IntSlice s) {
            this.s = s;
        }
        
        public void run() {
            s.registerNewOwner();
            Task<Void> leftTask = null;
            Task<Void> rightTask = null;
            
            int l = s.begin;
            int r = s.end - 1;
            if(s.length() > 2) {
                final int pivot = privot(s);
                while(l <= r) {
                    while(s.data[l] < pivot)
                        l++;
                    while(s.data[r] > pivot)
                        r--;
                    if(l <= r) {
                        final int temp = s.data[l];
                        s.data[l] = s.data[r];
                        s.data[r] = temp;
                        l++;
                        r--;
                    }
                }
                
                if(s.begin < r) {
                    final IntSlice left = s.slice(s.begin, r + 1);
                    left.pass();
                    leftTask = SYSTEM.run(new SortTask(left));
                }
                if(l < s.end - 1) {
                    final IntSlice right = s.slice(l, s.end);
                    right.pass();
                    rightTask = SYSTEM.run(new SortTask(right));
                }
            }
            else if(s.length() == 2) {
                /* Small optimization */
                if(s.data[l] > s.data[r]) {
                    final int temp = s.data[l];
                    s.data[l] = s.data[r];
                    s.data[r] = temp;
                }
            }
            /* else: s.length() == 1, already sorted */
            
            s.releasePassed();
            if(leftTask != null)
                leftTask.get();
            if(rightTask != null)
                rightTask.get();
        }
        
        private int privot(final @_UnguardedRead IntSlice slice) {
            return slice.data[slice.begin];
        }
    }
    
    private IntArray shuffledInts() {
        final IntArray array = new IntArray(new int[size]);
        final Random random = new Random();
        for(int i = 0; i < size; i++)
            array.data[i] = i;
        for(int i = size - 1; i > 0; i--) {
            final int index = random.nextInt(i + 1);
            
            final int t = array.data[index];
            array.data[index] = array.data[i];
            array.data[i] = t;
        }
        return array;
    }
}
