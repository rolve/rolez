package ch.trick17.peppl.manual.kmeans;

import static java.lang.Math.sqrt;

import java.util.concurrent.ThreadLocalRandom;

import ch.trick17.peppl.lib.guard.Array;
import ch.trick17.peppl.lib.guard.DoubleArray;
import ch.trick17.peppl.lib.guard.IntArray;
import ch.trick17.peppl.lib.task.TaskSystem;
import ch.trick17.peppl.lib.task.ThreadPoolTaskSystem;

public class KMeans implements Runnable {
    
    static final TaskSystem SYSTEM = new ThreadPoolTaskSystem();
    
    public static void main(final String[] args) {
        SYSTEM.runDirectly(new KMeans(2, 500, 20));
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
        
        final Array<DoubleArray> centroids = kMeans(dataSet);
        
        for(final DoubleArray centroid : centroids.data)
            System.out.println(tabbed(centroid));
    }
    
    private Array<DoubleArray> kMeans(final Array<DoubleArray> dataSet) {
        
        /* Initialization */
        final Array<DoubleArray> centroids = new Array<>(
                new DoubleArray[clusters]);
        for(int i = 0; i < clusters; i++)
            centroids.data[i] = randomVector();
        
        final IntArray assignments = new IntArray(new int[size]);
        
        /* Computation */
        boolean changed;
        do {
            /* Assignment step */
            changed = false;
            for(int v = 0; v < size; v++) {
                double min = Double.POSITIVE_INFINITY;
                int minIndex = -1;
                for(int c = 0; c < clusters; c++) {
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
            
            /* Update step */
            for(final DoubleArray centroid : centroids.data)
                for(int d = 0; d < dim; d++)
                    centroid.data[d] = 0;
            final int[] counts = new int[clusters];
            
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
    
    private Array<DoubleArray> createDataSet() {
        
        final DoubleArray[] dataSet = new DoubleArray[size];
        for(int i = 0; i < size; i++) {
            dataSet[i] = randomVector();
        }
        return new Array<>(dataSet);
    }
    
    private DoubleArray randomVector() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        
        final double[] vector = new double[dim];
        for(int d = 0; d < dim; d++)
            vector[d] = random.nextDouble(-100.0, 100.0);
        
        return new DoubleArray(vector);
    }
    
    private double distance(final DoubleArray v1, final DoubleArray v2) {
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
