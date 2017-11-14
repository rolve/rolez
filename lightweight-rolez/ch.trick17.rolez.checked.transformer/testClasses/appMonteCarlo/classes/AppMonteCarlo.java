package classes;

import java.util.Arrays;
import java.util.List;

import classes.RatePath;
import classes.Returns;
import rolez.checked.lang.CheckedArray;
import rolez.checked.lang.CheckedSlice;
import rolez.checked.lang.SliceRange;
import rolez.checked.lang.ContiguousPartitioner;
import rolez.checked.util.ArrayList;
import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;

@Checked
public class AppMonteCarlo {

	public String ratesFile = "Data/hitData";
	
	final public double pathStartValue = 100.0;
    
    public final int steps;
    public final int runs;
    public final int numTasks;
    final Returns returns;
    final CheckedArray<long[]> seeds;
    CheckedArray<double[]> results;
    
    public AppMonteCarlo() {
    	this.steps = 1000;
    	this.runs = 100000;
    	this.numTasks = 8;
    	RatePathReader ratePathReader = new RatePathReader();
    	this.returns = new Returns(ratePathReader.readRatesFile(ratesFile));
    	
    	this.seeds = new CheckedArray<long[]>(new long[runs]);
        for(int i = 0; i < runs; i++)
            seeds.setLong(i, i * 11);
        
        this.results = new CheckedArray<double[]>(new double[this.runs]);
    }
    
    public AppMonteCarlo(final int steps, final int runs, final int numTasks, String ratesFile) {
        this.steps = steps;
        this.runs = runs;
        this.numTasks = numTasks;
        RatePathReader ratePathReader = new RatePathReader();
    	this.returns = new Returns(ratePathReader.readRatesFile(ratesFile));

        this.seeds = new CheckedArray<long[]>(new long[runs]);
        for(int i = 0; i < runs; i++)
            seeds.setLong(i, i * 11);

        this.results = new CheckedArray<double[]>(new double[this.runs]);
    }
    
    public static void main(String[] args) {
    	String file = "Data/hitData";
    	int steps = 1000;
    	AppMonteCarlo app = new AppMonteCarlo(steps, 2000, 8, file);
    	app.run();
    	
    	// Uncomment this to see result -> test will fail
    	//System.out.println(app.avgExpectedReturnRate());
    }
    
    public void run() {
    	CheckedArray<CheckedSlice<long[]>[]> partitions = this.seeds.partition(ContiguousPartitioner.INSTANCE, this.numTasks);
    	CheckedArray<CheckedArray<double[]>[]> taskResults = new CheckedArray<CheckedArray<double[]>[]>(new CheckedArray[this.numTasks]);
    	
    	int partitionSize = ((CheckedSlice<long[]>)partitions.get(0)).getSliceRange().size();
    	
    	for (int i = 0; i < this.numTasks; i++) {
    		taskResults.set(i, new CheckedArray<double[]>(new double[partitionSize]));
    	}
    	
    	for(int i = 1; i < this.numTasks; i++) {
    		simulate((CheckedArray<double[]>)taskResults.slice(i,i+1).get(i), (CheckedSlice<long[]>)partitions.get(i), true);
    	}
    	
    	simulate((CheckedArray<double[]>)taskResults.slice(0,1).get(0), (CheckedSlice<long[]>)partitions.get(0), false);
    	
    	for (int i = 0; i < this.numTasks; i++) {
    		CheckedArray<double[]> taskResult = (CheckedArray<double[]>)taskResults.get(i);
    		for (int j = 0; j < taskResult.arrayLength(); j++) {
    			results.setDouble(i*partitionSize + j, taskResult.getDouble(j));
    		}
    	}
    }
    
    @Task
    public void simulate(@Readwrite CheckedArray<double[]> results, @Readonly CheckedSlice<long[]> seeds, boolean $asTask) {
    	for (int i = seeds.getSliceRange().begin; i < seeds.getSliceRange().end; i += seeds.getSliceRange().step) {
    		MonteCarloPath mcPath = new MonteCarloPath(this.returns, this.steps);
    		mcPath.computeFluctuations(seeds.getLong(i));
    		mcPath.computePathValues(this.pathStartValue);
    		results.setDouble(i-seeds.getSliceRange().begin, new Returns(mcPath).expectedReturnRate);
    	}
    }
    
    public double avgExpectedReturnRate() {
        double result = 0.0;
        for (int i = 0; i < this.runs; i++)
        	result += this.results.getDouble(i);
        result /= this.runs;
        return result;
    }
}
