package ch.trick17.peppl.manual.quicksort;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import ch.trick17.peppl.lib._UnguardedRead;
import ch.trick17.peppl.lib.guard.IntArray;
import ch.trick17.peppl.lib.guard.IntSlice;
import ch.trick17.peppl.lib.task.TaskSystem;
import ch.trick17.peppl.lib.task.ThreadPoolTaskSystem;

public class QuickSort implements Runnable {
    
    private static final TaskSystem SYSTEM = new ThreadPoolTaskSystem();
    private static final int MIN_TASK_SIZE = 10000;
    
    public static void main(final String[] args) {
        SYSTEM.runDirectly(new QuickSort(200000));
    }
    
    private final int size;
    
    public QuickSort(final int size) {
        this.size = size;
    }
    
    public void run() {
        final IntArray array = shuffledInts();
        
        final long start = System.nanoTime();
        
        array.pass();
        SYSTEM.run(new SortTask(array));
        array.guardRead();
        
        System.out.println((System.nanoTime() - start) / 1000000000.0);
        
        for(int i = 0; i < size; i++)
            assertEquals(i, array.data[i]);
    }
    
    public class SortTask implements Runnable {
        
        private final IntSlice s;
        
        public SortTask(final IntSlice s) {
            this.s = s;
        }
        
        public void run() {
            try {
                s.registerNewOwner();
                sort();
            } finally {
                s.releasePassed();
            }
        }
        
        public void sort() {
            int l = s.range.begin;
            int r = s.range.end - 1;
            if(s.size() > 2) {
                final int pivot = pivot(s);
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
                
                if(s.range.begin < r) {
                    final IntSlice left = s.slice(s.range.begin, r + 1, 1);
                    final SortTask task = new SortTask(left);
                    if(left.size() >= MIN_TASK_SIZE) {
                        left.pass();
                        SYSTEM.run(task);
                    }
                    else
                        task.sort();
                }
                if(l < s.range.end - 1) {
                    final int begin = l;
                    final IntSlice right = s.slice(begin, s.range.end, 1);
                    final SortTask task = new SortTask(right);
                    if(right.size() >= MIN_TASK_SIZE) {
                        right.pass();
                        SYSTEM.run(task);
                    }
                    else
                        task.sort();
                }
            }
            else if(s.size() == 2) {
                /* Small optimization */
                if(s.data[l] > s.data[r]) {
                    final int temp = s.data[l];
                    s.data[l] = s.data[r];
                    s.data[r] = temp;
                }
            }
            /* else: s.length() == 1, already sorted */
        }
        
        private int pivot(final @_UnguardedRead IntSlice slice) {
            // IMPROVE: Random pivot
            assert slice.size() > 0;
            final int l = slice.data[slice.range.begin];
            final int m = slice.data[slice.range.begin + slice.size() / 2];
            final int r = slice.data[slice.range.end - 1];
            if(l < m) {
                if(m < r)
                    return m;
                else if(l < r)
                    return l;
                else
                    return r;
            }
            else { // m < l
                if(l < r)
                    return l;
                else if(m < r)
                    return r;
                else
                    return m;
            }
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
