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
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 2001.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/

package section3.montecarlo;

import jgfutil.JGFInstrumentor;

public class JGFMonteCarloBench {
    
    private static final String DIR_NAME = "Data";
    private static final String FILE_NAME = "hitData";
    
    private static final int TIME_STEPS = 1000;
    private static final int DATA_SIZES[] = {10000, 60000};
    
    private final int nthreads;
    private final int size;
    private final int runs;
    
    private MonteCarloApp app = null;
    
    public JGFMonteCarloBench(final int nthreads, final int size) {
        this.nthreads = nthreads;
        this.size = size;
        this.runs = DATA_SIZES[size];
    }
    
    public void runAll() {
        JGFInstrumentor
                .addTimer("Section3:MonteCarlo:Total", "Solutions", size);
        JGFInstrumentor.addTimer("Section3:MonteCarlo:Run", "Samples", size);
        
        JGFInstrumentor.startTimer("Section3:MonteCarlo:Total");
        
        initialize();
        run();
        validate();
        
        JGFInstrumentor.stopTimer("Section3:MonteCarlo:Total");
        
        JGFInstrumentor.addOpsToTimer("Section3:MonteCarlo:Run", runs);
        JGFInstrumentor.addOpsToTimer("Section3:MonteCarlo:Total", 1);
        
        JGFInstrumentor.printTimer("Section3:MonteCarlo:Run");
        JGFInstrumentor.printTimer("Section3:MonteCarlo:Total");
    }
    
    public void initialize() {
        app = new MonteCarloApp(DIR_NAME, FILE_NAME, TIME_STEPS, runs, nthreads);
    }
    
    public void run() {
        JGFInstrumentor.startTimer("Section3:MonteCarlo:Run");
        app.runTasks();
        JGFInstrumentor.stopTimer("Section3:MonteCarlo:Run");
        
        app.processResults();
    }
    
    public void validate() {
        final double refval[] = {-0.0333976656762814, -0.03215796752868655};
        final double dev = Math.abs(app.JGFavgExpectedReturnRateMC
                - refval[size]);
        if(dev > 1.0e-12) {
            System.out.println("Validation failed");
            System.out
                    .println(" expectedReturnRate= "
                            + app.JGFavgExpectedReturnRateMC + "  " + dev
                            + "  " + size);
        }
    }
}
