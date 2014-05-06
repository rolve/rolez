/**************************************************************************
 *                                                                         *
 *         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
 *                                                                         *
 *                            produced by                                  *
 *                                                                         *
 *                  Java Grande Benchmarking Project                       *
 *                                                                         *
 *                                at                                       *
 *                                                                         *
 *                Edinburgh Parallel Computing Centre                      *
 *                                                                         *
 *                email: epcc-javagrande@epcc.ed.ac.uk                     *
 *                                                                         *
 *      Original version of this code by Hon Yau (hwyau@epcc.ed.ac.uk)     *
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 2001.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/

package ch.trick17.peppl.manual.jgfmontecarlo;

import static ch.trick17.peppl.lib.Partitioners.CONTIGUOUS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import section3.montecarlo.RatePath;
import section3.montecarlo.ReturnPath;
import ch.trick17.peppl.lib.guard.DoubleArray;
import ch.trick17.peppl.lib.guard.DoubleSlice;
import ch.trick17.peppl.lib.guard.LongArray;
import ch.trick17.peppl.lib.guard.LongSlice;
import ch.trick17.peppl.lib.task.Task;
import ch.trick17.peppl.lib.task.TaskSystem;

public class MonteCarloApp {
    
    private static final double pathStartValue = 100.0;
    
    private final int runs;
    private final int nthreads;
    
    private final PathParameters pathParams;
    
    public LongArray seeds;
    public DoubleArray results;
    
    public double JGFavgExpectedReturnRateMC = 0.0;
    
    public MonteCarloApp(final String dataDirname, final String dataFilename,
            final int timeSteps, final int runs, final int nthreads) {
        this.runs = runs;
        this.nthreads = nthreads;
        
        seeds = new LongArray(runs);
        results = new DoubleArray(runs);
        
        // Measure the requested path rate.
        final RatePath rateP = RatePath
                .readRatesFile(dataDirname, dataFilename);
        final ReturnPath returnP = rateP.getReturnCompounded();
        returnP.estimatePath();
        
        // Now prepare for MC runs.
        pathParams = new PathParameters(returnP, timeSteps);
        
        // Now create the seeds for the tasks.
        for(int i = 0; i < runs; i++)
            seeds.data[i] = i * 11;
    }
    
    public void runTasks() {
        final List<LongSlice> seedParts = seeds.partition(CONTIGUOUS, nthreads);
        final ArrayList<Task<List<Double>>> tasks = new ArrayList<>(nthreads);
        for(int i = 0; i < nthreads; i++) {
            pathParams.share();
            seedParts.get(i).share();
            tasks.add(TaskSystem.getDefault().run(
                    new RunTask(seedParts.get(i), pathParams)));
        }
        
        int index = 0;
        for(final Task<List<Double>> task : tasks) {
            final List<Double> taskResults = task.get();
            for(int i = 0; i < taskResults.size(); i++, index++)
                results.data[index] = taskResults.get(i);
        }
        
        if(index != runs)
            throw new AssertionError(
                    "Fatal: TaskRunner managed to finish with no all the results gathered in!");
    }
    
    public void processResults() {
        final List<DoubleSlice> resultParts = results.partition(CONTIGUOUS, nthreads);
        final ArrayList<Task<Double>> tasks = new ArrayList<>(nthreads);
        for(final DoubleSlice part : resultParts) {
            part.share();
            tasks.add(TaskSystem.getDefault().run(new SumTask(part)));
        }
        
        double avgExpectedReturnRateMC = 0.0;
        for(final Task<Double> task : tasks)
            avgExpectedReturnRateMC += task.get();
        
        avgExpectedReturnRateMC /= runs;
        JGFavgExpectedReturnRateMC = avgExpectedReturnRateMC;
    }
    
    private static class RunTask implements Callable<List<Double>> {
        
        private final List<Double> results = new ArrayList<>();
        private final LongSlice seeds;
        private final PathParameters pathParams;
        
        public RunTask(final LongSlice seeds, final PathParameters pathParams) {
            this.seeds = seeds;
            this.pathParams = pathParams;
        }
        
        public List<Double> call() {
            for(int i = seeds.begin; i < seeds.end; i++) {
                // pathParams.guardRead(); Compiler would optimize this
                final MonteCarloPath mcPath = new MonteCarloPath(pathParams);
                mcPath.computeFluctuationsGaussian(seeds.data[i]);
                mcPath.computePathValue(pathStartValue);
                final RatePath rateP = new RatePath(mcPath.get_name(), mcPath
                        .get_startDate(), mcPath.get_endDate(), mcPath
                        .get_dTime(), mcPath.get_pathValue());
                final ReturnPath returnP = rateP.getReturnCompounded();
                returnP.estimatePath();
                
                results.add(returnP.get_expectedReturnRate());
            }
            pathParams.releaseShared();
            return results;
        }
    }
    
    private static class SumTask implements Callable<Double> {
        
        private final DoubleSlice data;
        
        public SumTask(final DoubleSlice data) {
            this.data = data;
        }
        
        public Double call() {
            double sum = 0;
            for(int i = data.begin; i < data.end; i++) {
                sum += data.data[i];
            }
            data.releaseShared();
            return sum;
        }
    }
}
