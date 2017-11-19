package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.checked.lang.CheckedSlice;
import rolez.checked.lang.CheckedVectorBuilder;
import rolez.checked.lang.ContiguousPartitioner;
import rolez.checked.lang.SliceRange;
import rolez.checked.lang.Vector;
import rolez.checked.lang.CheckedArray;
import rolez.checked.lang.VectorBuilder;
import rolez.checked.util.StopWatch;

import java.util.Random;

@Checked
public class AppKmeans {

	final int dim;
	final int clusters;
	final int numTasks;
	
	public AppKmeans() {
		this.dim = 10;
		this.clusters = 10;
		this.numTasks = 8;
	}
	
	public AppKmeans(int dim, int clusters, int numTasks) {
		this.dim = dim;
		this.clusters = clusters;
		this.numTasks = numTasks;
	}

	public static void main(String[] args) {
		int n = 20000;
		int clusters = 10;
		int dim = 10;
		int maxIterations = 50;
		int repetitions = 100;
		int numTasks = 4;
		StopWatch sw = new StopWatch();
		
		AppKmeans instance = new AppKmeans(dim, clusters, numTasks);
		CheckedArray<CheckedArray<Vector<double[]>[]>[]> dataSets = new CheckedArray(new CheckedArray[repetitions]);
		for (int i = 0; i < repetitions; i++) {
			dataSets.set(i, instance.createDataSet(n, new Random()));
		}
		
		for (int i = 0; i < repetitions; i++) {
			sw.go();
			instance.kMeans((CheckedArray<double[][]>)dataSets.get(i), maxIterations);
			//System.out.println(sw.get());
		}
	}
	
	public CheckedArray<double[][]> createDataSet(int n, Random random) {
		CheckedArray<double[][]> dataSet = new CheckedArray(new double[n][]);
		for (int i = 0; i < n; i++) {
			dataSet.set(i, this.newRandomVector(random));
		}
		return dataSet;
	}
	
	public double[] newRandomVector(Random random) {
		CheckedVectorBuilder<double[]> vec = new CheckedVectorBuilder(new double[this.dim]);
		for (int d = 0; d < this.dim; d++)
			vec.setDouble(d, random.nextDouble());
		return vec.build().getData();
	}
	
	public CheckedArray<double[][]> kMeans(CheckedArray<double[][]> dataSet, int maxIterations) {
		Random random = new Random();
		CheckedArray<double[][]> centroids = new CheckedArray(new double[this.clusters][]);
		for (int i = 0; i < this.clusters; i++) {
			centroids.set(i, this.newRandomVector(random));
		}
		
		CheckedArray<int[]> assignments = new CheckedArray(new int[dataSet.arrayLength()]);
		
		// Computation
		int iterations = 0;
		boolean changed = true;
		
		while (changed && iterations < maxIterations) {
			CheckedArray<CheckedSlice<double[][]>[]> dataParts = dataSet.partition(ContiguousPartitioner.INSTANCE, this.numTasks);
			CheckedArray<CheckedSlice<int[]>[]> assignParts = assignments.partition(ContiguousPartitioner.INSTANCE, this.numTasks);
			CheckedArray<Result[]> results = new CheckedArray(new Result[this.numTasks]);
			for (int i = 0; i < this.numTasks; i++)
				results.set(i, new Result());
			
			CheckedArray<CheckedSlice<Result[]>[]> resultParts = results.partition(ContiguousPartitioner.INSTANCE, this.numTasks);
			
			for (int i = 0; i < this.numTasks; i++) {
				this.assignAndUpdate((CheckedSlice<Result[]>)resultParts.get(i), (CheckedSlice<double[][]>)dataParts.get(i), centroids, (CheckedSlice<int[]>)assignParts.get(i), true);
			}
			
			changed = false;
			CheckedArray<CheckedVectorBuilder<double[]>[]> newCentroids = new CheckedArray<CheckedVectorBuilder<double[]>[]>(new CheckedVectorBuilder[this.clusters]);
			for (int i = 0; i < newCentroids.arrayLength(); i++)
				newCentroids.set(i, new CheckedVectorBuilder<double[]>(new double[this.dim]));
			CheckedArray<int[]> counts = new CheckedArray<int[]>(new int[this.clusters]);
			
			
			for (int i = 0; i < this.numTasks; i++) {
				Result result = (Result)results.get(i);
				changed |= result.changed;
				for (int c = 0; c < this.clusters; c++) {
					CheckedVectorBuilder<double[]> newCentroid = (CheckedVectorBuilder<double[]>)newCentroids.get(c);
					double[] resultCentroid = result.centroids[c];
					for (int d = 0; d < this.dim; d++)
						newCentroid.setDouble(d, newCentroid.data[d] + resultCentroid[d]);
					counts.setInt(c, counts.getInt(c) + result.counts[c]);
				}
			}
			
			for (int c = 0; c < this.clusters; c++) {
				CheckedVectorBuilder<double[]> centroid = (CheckedVectorBuilder<double[]>)newCentroids.get(c);
				int count = counts.getInt(c);
				for (int d = 0; d < this.dim; d++)
					centroid.setDouble(d, centroid.data[d] / count);
				centroids.set(c, centroid.build().getData());
			}
			iterations++;
		}
		
		return centroids;
	}
	
	@Task
	public void assignAndUpdate(@Readwrite CheckedSlice<Result[]> results,
						        @Readonly CheckedSlice<double[][]> dataSet,
						        @Readonly CheckedArray<double[][]> centroids,
						        @Readwrite CheckedSlice<int[]> assignments,
						        boolean $asTask) {
		boolean changed = false;
		CheckedArray<CheckedVectorBuilder<double[]>[]> newCentroids = new CheckedArray(new CheckedVectorBuilder[this.clusters]);
		for (int i = 0; i < this.clusters; i++)
			newCentroids.set(i, new CheckedVectorBuilder<double[]>(new double[this.dim]));
		CheckedVectorBuilder<int[]> counts = new CheckedVectorBuilder<int[]>(new int[centroids.arrayLength()]);
		
		int begin = dataSet.getSliceRange().begin;
		int end = dataSet.getSliceRange().end;
		int step = dataSet.getSliceRange().step;
		for (int i = begin; i < end; i += step) {
			double[] vector = (double[])dataSet.get(i);
			double min = Double.POSITIVE_INFINITY;
			int cluster = -1;
			for (int c = 0; c < centroids.arrayLength(); c++) {
				double distance2 = this.distance2(vector, (double[])centroids.get(c));
				if (distance2 < min) {
					min = distance2;
					cluster = c;
				}
			}
			
			if (cluster != assignments.getInt(i)) {
				changed = true;
				assignments.setInt(i, cluster);
			}
			
			CheckedVectorBuilder<double[]> newCentroid = (CheckedVectorBuilder<double[]>)newCentroids.get(cluster);
			for (int d = 0; d < vector.length; d++)
				newCentroid.setDouble(d, newCentroid.data[d] + vector[d]);
			counts.setInt(cluster, counts.data[cluster] + 1);
		}
		Result result = (Result)results.get(results.getSliceRange().begin);
		result.setChanged(changed);
		result.setCentroids(newCentroids);
		result.setCounts(counts.build().getData());
	}
	
	public double distance2(double[] v1, double[] v2) {
		double sum = 0.0;
		for (int d = 0; d < this.dim; d++) {
			double diff = v1[d] - v2[d];
			sum += diff * diff;
		}
		return sum;
	}
}

@Checked
class Result {
    boolean changed;
    double[][] centroids;
    int[] counts;
        
    public void setChanged(boolean changed) {
    	this.changed = changed;
    }
    
    public void setCentroids(CheckedArray<CheckedVectorBuilder<double[]>[]> centroids) {
    	CheckedVectorBuilder<double[][]> centroidsBuilder = new CheckedVectorBuilder<double[][]>(new double[centroids.arrayLength()][]);
        for(int c = 0; c < centroids.arrayLength(); c++) {
            centroidsBuilder.set(c, ((CheckedVectorBuilder<double[]>)centroids.get(c)).build().getData());
        }
        this.centroids = centroidsBuilder.build().getData();
    }
    
    public void setCounts(int[] counts) {
    	this.counts = counts;
    }
}
