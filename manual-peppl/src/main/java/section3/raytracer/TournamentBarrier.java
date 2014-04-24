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

package section3.raytracer;

// This implements a simple tournament-based barrier, using entirely its
// own synchronisation. At present Yield() is called to stop busy-waiting
// processes hogging the processor(s)!

public class TournamentBarrier extends Barrier {
    
    private static final int maxBusyIter = 1;
    
    // Array of flags indicating whether the given process and all those
    // for which it is responsible have finished. The "sense" of this
    // array alternates with each barrier, to prevent having to
    // reinitialise.
    private volatile boolean[] isDone;
    
    public TournamentBarrier(final int n) {
        // Superclass constructor should record the number of threads
        // and thread manager.
        super(n);
        
        // Initialise the IsDone array. The choice of initial value is
        // arbitrary, but must be consistent!
        isDone = new boolean[numThreads];
        for(int i = 0; i < n; i++) {
            isDone[i] = false;
        }
    }
    
    @Override
    public void doBarrier(final int myid) {
        int b;
        // debug("Thread " + myid + " checking in");
        
        int roundmask = 3;
        final boolean donevalue = !isDone[myid];
        
        while(((myid & roundmask) == 0) && (roundmask < (numThreads << 2))) {
            final int spacing = (roundmask + 1) >> 2;
            for(int i = 1; i <= 3 && myid + i * spacing < numThreads; i++) {
                // debug("Thread " + myid + " waiting for thread " +
                // (myid+i*spacing));
                b = maxBusyIter;
                while(isDone[myid + i * spacing] != donevalue) {
                    b--;
                    if(b == 0) {
                        Thread.yield();
                        b = maxBusyIter;
                    }
                }
            }
            roundmask = (roundmask << 2) + 3;
        }
        // debug("Thread " + myid + " reporting done");
        isDone[myid] = donevalue;
        b = maxBusyIter;
        while(isDone[0] != donevalue) {
            b--;
            if(b == 0) {
                Thread.yield();
                b = maxBusyIter;
            }
        }
        // debug("Thread " + myid + " checking out");
        
    }
}
