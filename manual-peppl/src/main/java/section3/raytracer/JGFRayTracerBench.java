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
    
    public int nthreads;
    public static long checksum = 0;
    public static int staticnumobjects;
    
    public static final int[] datasizes = {150, 500};
    
    private final int size;
    private final int width;
    private int height;
    
    public JGFRayTracerBench(final int nthreads, final int size) {
        this.nthreads = nthreads;
        this.size = size;
        
        width = height = datasizes[size];
    }
    
    public void JGFapplication() {
        
        final Runnable thobjects[] = new Runnable[nthreads];
        final Thread th[] = new Thread[nthreads];
        final Barrier br = new TournamentBarrier(nthreads);
        
        // Start Threads
        
        for(int i = 1; i < nthreads; i++) {
            
            thobjects[i] = new RayTracerRunner(i, width, height, br);
            th[i] = new Thread(thobjects[i]);
            th[i].start();
        }
        
        thobjects[0] = new RayTracerRunner(0, width, height, br);
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
        
        JGFInstrumentor.addOpsToTimer("Section3:RayTracer:Init",
                staticnumobjects);
        JGFInstrumentor.addOpsToTimer("Section3:RayTracer:Run", width * height);
        JGFInstrumentor.addOpsToTimer("Section3:RayTracer:Total", 1);
        
        JGFInstrumentor.printTimer("Section3:RayTracer:Init");
        JGFInstrumentor.printTimer("Section3:RayTracer:Run");
        JGFInstrumentor.printTimer("Section3:RayTracer:Total");
    }
    
    private class RayTracerRunner extends RayTracer implements Runnable {
        
        int id, height, width;
        Barrier br;
        
        public RayTracerRunner(final int id, final int width, final int height,
                final Barrier br) {
            this.id = id;
            this.width = width;
            this.height = height;
            this.br = br;
            
            JGFInstrumentor.startTimer("Section3:RayTracer:Init");
            
            // create the objects to be rendered
            final Scene scene = Scene.createScene();
            
            // get lights, objects etc. from scene.
            setScene(scene);
            
            numobjects = scene.getObjects();
            JGFRayTracerBench.staticnumobjects = numobjects;
            
            JGFInstrumentor.stopTimer("Section3:RayTracer:Init");
            
        }
        
        public void run() {
            
            // Set interval to be rendered to the whole picture
            // (overkill, but will be useful to retain this for parallel
            // versions)
            
            final Interval interval = new Interval(width, height, 0, height, id);
            
            // synchronise threads and start timer
            
            br.doBarrier(id);
            if(id == 0)
                JGFInstrumentor.startTimer("Section3:RayTracer:Run");
            
            render(interval, nthreads);
            
            // Signal this thread has done iteration
            
            synchronized(JGFRayTracerBench.class) {
                for(int i = 0; i < nthreads; i++)
                    if(id == i)
                        JGFRayTracerBench.checksum = JGFRayTracerBench.checksum
                                + checksum;
            }
            
            // synchronise threads and stop timer
            
            br.doBarrier(id);
            if(id == 0)
                JGFInstrumentor.stopTimer("Section3:RayTracer:Run");
        }
    }
    
}
