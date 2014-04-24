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

public class JGFMonteCarloBenchSizeB {
    
    public static void main(final String argv[]) {
        int nthreads;
        if(argv.length != 0)
            nthreads = Integer.parseInt(argv[0]);
        else {
            System.out
                    .println("The no of threads has not been specified, defaulting to 1\n");
            nthreads = 1;
        }
        
        JGFInstrumentor.printHeader(3, 1, nthreads);
        new JGFMonteCarloBench(nthreads, 1).runAll();
    }
}
