package ch.trick17.peppl.manual.kmeans;

import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import ch.trick17.peppl.lib.Mutable;
import ch.trick17.peppl.lib._Mutable;
import ch.trick17.peppl.lib.guard.Array;
import ch.trick17.peppl.lib.guard.DoubleArray;
import ch.trick17.peppl.lib.guard.IntArray;
import ch.trick17.peppl.lib.guard.IntSlice;
import ch.trick17.peppl.lib.guard.Slice;
import ch.trick17.peppl.lib.task.Task;
import ch.trick17.peppl.lib.task.TaskSystem;
import ch.trick17.peppl.lib.task.ThreadPoolTaskSystem;

public class KMeans implements Runnable {
    
    static final TaskSystem SYSTEM = new ThreadPoolTaskSystem();
    private static final int PARTS = 32;
    
    public static void main(final String[] args) {
        SYSTEM.runDirectly(new KMeans(10, 50000, 20));
    }
    
    private final int dim;
    private final int size;
    private final int clusters;
    
    public KMeans(final int dim, final int size, final int clusters) {
        this.dim = dim;
        this.size = size;
        this.clusters = clusters;
    }
    
    public void run() {
        final Array<DoubleArray> dataSet = createDataSet();
        
        long time = System.currentTimeMillis();
        final Array<DoubleArray> centroids = kMeans(dataSet);
        time = System.currentTimeMillis() - time;
        
        // for(final DoubleArray centroid : centroids.data)
        // System.out.println(tabbed(centroid));
        
        System.out.println("\n" + time);
    }
    
    private Array<DoubleArray> kMeans(final Array<DoubleArray> dataSet) {
        
        /* Initialization */
        final Array<DoubleArray> centroids = new Array<>(
                new DoubleArray[clusters]);
        for(int i = 0; i < clusters; i++)
            centroids.data[i] = randomVector();
        
        final IntArray assignments = new IntArray(size);
        
        /* Computation */
        boolean changed;
        do {
            /* Assignment step */
            final List<Slice<DoubleArray>> dataParts = dataSet.partition(PARTS);
            final List<IntSlice> assignParts = assignments.partition(PARTS);
            final List<Task<Boolean>> tasks = new ArrayList<>(PARTS);
            
            for(int i = 0; i < PARTS; i++) {
                final Slice<DoubleArray> dataPart = dataParts.get(i);
                final IntSlice assignPart = assignParts.get(i);
                dataPart.share(); // Partitioning would not be necessary
                centroids.share();
                assignPart.pass();
                tasks.add(SYSTEM.run(new AssignmentTask(dataPart, centroids,
                        assignPart)));
            }
            
            changed = false;
            for(final Task<Boolean> task : tasks)
                changed |= task.get();
            
            /* Update step */
            centroids.guardReadWrite(); // Theoretically not necessary, tasks
                                        // are guaranteed to be finished...
            for(final DoubleArray centroid : centroids.data)
                for(int d = 0; d < dim; d++)
                    centroid.data[d] = 0;
            final int[] counts = new int[clusters];
            
            assignments.guardRead(); // Theoretically not necessary, tasks
                                     // are guaranteed to be finished...
            for(int v = 0; v < size; v++) {
                final DoubleArray vector = dataSet.data[v];
                final int index = assignments.data[v];
                final DoubleArray centroid = centroids.data[index];
                for(int d = 0; d < dim; d++)
                    centroid.data[d] += vector.data[d];
                counts[index]++;
            }
            
            for(int c = 0; c < clusters; c++) {
                final DoubleArray centroid = centroids.data[c];
                final double count = counts[c];
                for(int d = 0; d < dim; d++)
                    centroid.data[d] /= count;
            }
        } while(changed);
        
        return centroids;
    }
    
    static class AssignmentTask implements Callable<Boolean> {
        
        private final Slice<DoubleArray> dataSet;
        private final Array<DoubleArray> centroids;
        private final @_Mutable IntSlice assignments;
        
        AssignmentTask(final Slice<DoubleArray> dataSet,
                final Array<DoubleArray> centroids,
                @Mutable final IntSlice assignments) {
            this.dataSet = dataSet;
            this.centroids = centroids;
            this.assignments = assignments;
        }
        
        public Boolean call() throws Exception {
            try {
                assignments.registerNewOwner();
                return assignmentStep(dataSet, centroids, assignments);
            } finally {
                dataSet.releaseShared();
                centroids.releaseShared();
                assignments.releasePassed();
            }
        }
        
    }
    
    private static boolean assignmentStep(final Slice<DoubleArray> dataSet,
            final Array<DoubleArray> centroids,
            @Mutable final IntSlice assignments) {
        boolean changed = false;
        dataSet.guardRead();
        centroids.guardRead();
        assignments.guardReadWrite();
        for(int v = dataSet.begin; v < dataSet.end; v++) {
            double min = Double.POSITIVE_INFINITY;
            int minIndex = -1;
            for(int c = 0; c < centroids.length(); c++) {
                final double distance = distance(dataSet.data[v],
                        centroids.data[c]);
                if(distance < min) {
                    min = distance;
                    minIndex = c;
                }
            }
            if(minIndex != assignments.data[v]) {
                changed = true;
                assignments.data[v] = minIndex;
            }
        }
        return changed;
    }
    
    private Array<DoubleArray> createDataSet() {
        
        final DoubleArray[] dataSet = new DoubleArray[size];
        for(int i = 0; i < size; i++) {
            dataSet[i] = randomVector();
        }
        return new Array<>(dataSet);
    }
    
    private DoubleArray randomVector() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        
        final DoubleArray vector = new DoubleArray(dim);
        for(int d = 0; d < dim; d++)
            vector.data[d] = random.nextDouble(-100.0, 100.0);
        
        return vector;
    }
    
    private static double distance(final DoubleArray v1, final DoubleArray v2) {
        final int dim = v1.length();
        assert v2.length() == dim;
        double sum = 0;
        for(int d = 0; d < dim; d++) {
            final double diff = v1.data[d] - v2.data[d];
            sum += diff * diff;
        }
        return sqrt(sum);
    }
    
    private CharSequence tabbed(final DoubleArray vector) {
        final StringBuilder builder = new StringBuilder(Double
                .toString(vector.data[0]));
        for(int d = 1; d < dim; d++)
            builder.append('\t').append(vector.data[d]);
        return builder;
    }
}
