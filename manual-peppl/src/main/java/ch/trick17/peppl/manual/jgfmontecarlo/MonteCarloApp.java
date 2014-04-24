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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import section3.montecarlo.RatePath;
import section3.montecarlo.ReturnPath;
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
    public List<Double> results;
    
    public double JGFavgExpectedReturnRateMC = 0.0;
    
    public MonteCarloApp(final String dataDirname, final String dataFilename,
            final int timeSteps, final int runs, final int nthreads) {
        this.runs = runs;
        this.nthreads = nthreads;
        
        seeds = new LongArray(runs);
        results = new ArrayList<>(runs);
        
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
        final List<LongSlice> seedParts = seeds.partition(nthreads);
        final ArrayList<Task<List<Double>>> tasks = new ArrayList<>(nthreads);
        for(int i = 0; i < nthreads; i++) {
            pathParams.share();
            seedParts.get(i).share();
            tasks.add(TaskSystem.getDefault().run(
                    new AppDemoTask(seedParts.get(i), pathParams)));
        }
        
        for(final Task<List<Double>> task : tasks)
            results.addAll(task.get());
    }
    
    public void processResults() {
        double avgExpectedReturnRateMC = 0.0;
        if(runs != results.size())
            throw new AssertionError(
                    "Fatal: TaskRunner managed to finish with no all the results gathered in!");
        
        for(int i = 0; i < runs; i++)
            avgExpectedReturnRateMC += results.get(i);
        
        avgExpectedReturnRateMC /= runs;
        JGFavgExpectedReturnRateMC = avgExpectedReturnRateMC;
    }
    
    private static class AppDemoTask implements Callable<List<Double>> {
        
        private final List<Double> results = new ArrayList<>();
        private final LongSlice seeds;
        private final PathParameters pathParams;
        
        public AppDemoTask(final LongSlice seeds,
                final PathParameters pathParams) {
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
}
