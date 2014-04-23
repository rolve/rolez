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

package section3.montecarlo;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Code, a test-harness for invoking and driving the Applications Demonstrator
 * classes.
 * <p>
 * To do:
 * <ol>
 * <li>Very long delay prior to connecting to the server.</li>
 * <li>Some text output seem to struggle to get out, without the user tapping
 * ENTER on the keyboard!</li>
 * </ol>
 *
 * @author H W Yau
 * @version $Revision: 1.12 $ $Date: 1999/02/16 19:13:38 $
 */
public class AppDemo extends Universal {
    
    private static final double pathStartValue = 100.0;
    
    public double JGFavgExpectedReturnRateMC = 0.0;
    private final int nRunsMC;
    private final int nthreads;
    
    private ToInitAllTasks initAllTasks = null;
    
    public List<Long> seeds;
    public List<Double> results;
    
    public AppDemo(final String dataDirname, final String dataFilename,
            final int nTimeStepsMC, final int nRunsMC, final int nthreads) {
        this.nRunsMC = nRunsMC;
        this.nthreads = nthreads;
        
        seeds = new ArrayList<>(nRunsMC);
        results = new ArrayList<>(nRunsMC);
        
        set_prompt("AppDemo> ");
        set_DEBUG(true);
        
        try {
            // Measure the requested path rate.
            final RatePath rateP = new RatePath(dataDirname, dataFilename);
            final ReturnPath returnP = rateP.getReturnCompounded();
            returnP.estimatePath();
            
            // Now prepare for MC runs.
            initAllTasks = new ToInitAllTasks(returnP, nTimeStepsMC,
                    pathStartValue);
            
            // Now create the seeds for the tasks.
            for(int i = 0; i < nRunsMC; i++)
                seeds.add((long) i * 11);
            
        } catch(final DemoException demoEx) {
            dbgPrintln(demoEx.toString());
            System.exit(-1);
        }
    }
    
    public void runTasks() {
        final AppDemoTask tasks[] = new AppDemoTask[nthreads];
        final Thread threads[] = new Thread[nthreads];
        for(int i = 1; i < nthreads; i++) {
            tasks[i] = new AppDemoTask(i, nRunsMC, nthreads, seeds,
                    initAllTasks);
            threads[i] = new Thread(tasks[i]);
            threads[i].start();
        }
        
        final AppDemoTask task = new AppDemoTask(0, nRunsMC, nthreads, seeds,
                initAllTasks);
        task.run();
        results.addAll(task.getResults());
        
        for(int i = 1; i < nthreads; i++) {
            try {
                threads[i].join();
                results.addAll(tasks[i].getResults());
            } catch(final InterruptedException e) {}
        }
    }
    
    public void processSerial() {
        //
        // Process the results.
        try {
            processResults();
        } catch(final DemoException demoEx) {
            dbgPrintln(demoEx.toString());
            System.exit(-1);
        }
    }
    
    /**
     * Method for doing something with the Monte Carlo simulations. It's
     * probably not mathematically correct, but shall take an average over all
     * the simulated rate paths.
     *
     * @exception DemoException
     *                thrown if there is a problem with reading in any values.
     */
    private void processResults() throws DemoException {
        double avgExpectedReturnRateMC = 0.0;
        if(nRunsMC != results.size()) {
            errPrintln("Fatal: TaskRunner managed to finish with no all the results gathered in!");
            System.exit(-1);
        }
        
        for(int i = 0; i < nRunsMC; i++)
            avgExpectedReturnRateMC += results.get(i);
        
        avgExpectedReturnRateMC /= nRunsMC;
        JGFavgExpectedReturnRateMC = avgExpectedReturnRateMC;
    }
    
    private static class AppDemoTask implements Runnable {
        
        private final int id, nRunsMC, nthreads;
        private final List<Double> results = new ArrayList<>();
        private final List<Long> seeds;
        private final ToInitAllTasks initAllTasks;
        
        public AppDemoTask(final int id, final int nRunsMC, final int nthreads,
                final List<Long> seeds, final ToInitAllTasks initAllTasks) {
            this.id = id;
            this.nRunsMC = nRunsMC;
            this.nthreads = nthreads;
            this.seeds = seeds;
            this.initAllTasks = initAllTasks;
        }
        
        public void run() {
            int ilow, iupper, slice;
            slice = (nRunsMC + nthreads - 1) / nthreads;
            ilow = id * slice;
            iupper = (id + 1) * slice;
            if(id == nthreads - 1)
                iupper = nRunsMC;
            
            for(int iRun = ilow; iRun < iupper; iRun++) {
                final PriceStock ps = new PriceStock();
                ps.setInitAllTasks(initAllTasks);
                ps.setSeed(seeds.get(iRun));
                ps.run();
                results.add(ps.getExpectedReturnRate());
            }
        }
        
        public List<Double> getResults() {
            return unmodifiableList(results);
        }
    }
}
