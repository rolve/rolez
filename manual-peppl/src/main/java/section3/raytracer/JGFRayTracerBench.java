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

import jgfutil.JGFInstrumentor;

public class JGFRayTracerBench {
    
    public static final int[] datasizes = {150, 500};
    
    private final int nthreads;
    private final int size;
    private final int width;
    private final int height;
    
    public long checksum = 0;
    public int objectCount = 0;
    
    public JGFRayTracerBench(final int nthreads, final int size) {
        this.nthreads = nthreads;
        this.size = size;
        width = height = datasizes[size];
    }
    
    public void JGFapplication() {
        
        final Runnable thobjects[] = new Runnable[nthreads];
        final Barrier br = new TournamentBarrier(nthreads);
        
        JGFInstrumentor.startTimer("Section3:RayTracer:Init");
        final Scene scene = Scene.createScene();
        JGFInstrumentor.stopTimer("Section3:RayTracer:Init");
        objectCount += scene.objects.length;
        
        // Create tasks
        for(int i = 0; i < nthreads; i++)
            thobjects[i] = new RayTracerRunner(scene, i, br);
        
        // Start Threads
        final Thread th[] = new Thread[nthreads];
        for(int i = 1; i < nthreads; i++) {
            th[i] = new Thread(thobjects[i]);
            th[i].start();
        }
        thobjects[0].run();
        
        for(int i = 1; i < nthreads; i++) {
            try {
                th[i].join();
            } catch(final InterruptedException e) {}
        }
    }
    
    public void JGFvalidate() {
        final long refval[] = {2676692, 29827635};
        final long dev = checksum - refval[size];
        if(dev != 0) {
            System.out.println("Validation failed");
            System.out.println("Pixel checksum = " + checksum);
            System.out.println("Reference value = " + refval[size]);
        }
    }
    
    public void JGFrun() {
        JGFInstrumentor.addTimer("Section3:RayTracer:Total", "Solutions", size);
        JGFInstrumentor.addTimer("Section3:RayTracer:Init", "Objects", size);
        JGFInstrumentor.addTimer("Section3:RayTracer:Run", "Pixels", size);
        
        JGFInstrumentor.startTimer("Section3:RayTracer:Total");
        
        JGFapplication();
        JGFvalidate();
        
        JGFInstrumentor.stopTimer("Section3:RayTracer:Total");
        
        JGFInstrumentor.addOpsToTimer("Section3:RayTracer:Init", objectCount);
        JGFInstrumentor.addOpsToTimer("Section3:RayTracer:Run", width * height);
        JGFInstrumentor.addOpsToTimer("Section3:RayTracer:Total", 1);
        
        JGFInstrumentor.printTimer("Section3:RayTracer:Init");
        JGFInstrumentor.printTimer("Section3:RayTracer:Run");
        JGFInstrumentor.printTimer("Section3:RayTracer:Total");
    }
    
    private class RayTracerRunner extends RayTracer implements Runnable {
        
        int id;
        Barrier br;
        
        public RayTracerRunner(final Scene scene, final int id, final Barrier br) {
            super(scene);
            this.id = id;
            this.br = br;
        }
        
        public void run() {
            // synchronise threads and start timer
            
            br.doBarrier(id);
            if(id == 0)
                JGFInstrumentor.startTimer("Section3:RayTracer:Run");
            
            final long check = render(
                    new Interval(width, height, 0, height, id), nthreads);
            
            // Signal this thread has done iteration
            
            synchronized(JGFRayTracerBench.this) {
                for(int i = 0; i < nthreads; i++)
                    if(id == i)
                        checksum += check;
            }
            
            // synchronise threads and stop timer
            
            br.doBarrier(id);
            if(id == 0)
                JGFInstrumentor.stopTimer("Section3:RayTracer:Run");
        }
    }
}
