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
    
    private final int[][] image;
    
    public int objectCount = 0;
    
    public JGFRayTracerBench(final int nthreads, final int size) {
        this.nthreads = nthreads;
        this.size = size;
        image = new int[datasizes[size]][datasizes[size]];
    }
    
    public void JGFapplication() {
        final RayTracerRunner[] thobjects = new RayTracerRunner[nthreads];
        
        JGFInstrumentor.startTimer("Section3:RayTracer:Init");
        final Scene scene = Scene.createScene();
        JGFInstrumentor.stopTimer("Section3:RayTracer:Init");
        objectCount += scene.objects.length;
        
        // Create tasks
        for(int i = 0; i < nthreads; i++)
            thobjects[i] = new RayTracerRunner(scene, i);
        
        // Start Threads
        JGFInstrumentor.startTimer("Section3:RayTracer:Run");
        final Thread[] th = new Thread[nthreads];
        for(int i = 1; i < nthreads; i++) {
            th[i] = new Thread(thobjects[i]);
            th[i].start();
        }
        
        thobjects[0].run();
        
        for(int i = 1; i < nthreads; i++)
            try {
                th[i].join();
            } catch(final InterruptedException e) {}
        JGFInstrumentor.stopTimer("Section3:RayTracer:Run");
    }
    
    public void JGFvalidate() {
        long checksum = 0;
        for(int y = 0; y < image.length; y++)
            for(int x = 0; x < image[0].length; x++) {
                final int color = image[y][x];
                final int r = (color & 0xffffff) >> 16;
                final int g = (color & 0xffff) >> 8;
                final int b = (color & 0xff);
                checksum += r + g + b;
            }
        
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
        JGFInstrumentor.addOpsToTimer("Section3:RayTracer:Run", image.length
                * image[0].length);
        JGFInstrumentor.addOpsToTimer("Section3:RayTracer:Total", 1);
        
        JGFInstrumentor.printTimer("Section3:RayTracer:Init");
        JGFInstrumentor.printTimer("Section3:RayTracer:Run");
        JGFInstrumentor.printTimer("Section3:RayTracer:Total");
    }
    
    private class RayTracerRunner implements Runnable {
        
        private final Scene scene;
        private final int id;
        
        public RayTracerRunner(final Scene scene, final int id) {
            this.scene = scene;
            this.id = id;
        }
        
        public void run() {
            final RayTracer tracer = new RayTracer(scene);
            tracer.render(image, id, nthreads);
        }
    }
}
