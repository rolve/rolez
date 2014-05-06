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

package ch.trick17.peppl.manual.raytracer;

import static ch.trick17.peppl.lib.Partitioners.STRIPED;

import java.util.List;

import jgfutil.JGFInstrumentor;
import ch.trick17.peppl.lib.Mutable;
import ch.trick17.peppl.lib._Mutable;
import ch.trick17.peppl.lib.guard.Array;
import ch.trick17.peppl.lib.guard.IntArray;
import ch.trick17.peppl.lib.guard.Slice;
import ch.trick17.peppl.lib.task.NewThreadTaskSystem;
import ch.trick17.peppl.lib.task.Task;
import ch.trick17.peppl.lib.task.TaskSystem;

public class JGFRayTracerBench {
    
    public static final int[] datasizes = {150, 500};
    
    private final int nthreads;
    private final int size;
    
    private final Array<IntArray> image;
    
    public int objectCount;
    
    public JGFRayTracerBench(final int nthreads, final int size) {
        this.nthreads = nthreads;
        this.size = size;
        image = new Array<IntArray>(new IntArray[datasizes[size]]);
        for(int i = 0; i < datasizes[size]; i++)
            image.data[i] = new IntArray(new int[datasizes[size]]);
    }
    
    public void JGFapplication() {
        JGFInstrumentor.startTimer("Section3:RayTracer:Init");
        final Scene scene = Scene.createScene();
        objectCount = scene.objects.length();
        JGFInstrumentor.stopTimer("Section3:RayTracer:Init");
        
        JGFInstrumentor.startTimer("Section3:RayTracer:Run");
        final TaskSystem taskSystem = new NewThreadTaskSystem();
        
        final List<Slice<IntArray>> imageParts = image.partition(STRIPED, nthreads);
        final Task<?>[] tasks = new Task<?>[nthreads];
        for(int i = 1; i < nthreads; i++) {
            final Slice<IntArray> part = imageParts.get(i);
            part.pass();
            tasks[i] = taskSystem.run(new RayTracerTask(part, scene));
        }
        
        imageParts.get(0).pass();
        taskSystem.runDirectly(new RayTracerTask(imageParts.get(0), scene));
        
        for(int i = 1; i < nthreads; i++)
            tasks[i].get();
        
        JGFInstrumentor.stopTimer("Section3:RayTracer:Run");
    }
    
    public void JGFvalidate() {
        long checksum = 0;
        image.guardRead();
        for(int y = 0; y < image.size(); y++) {
            image.data[y].guardRead();
            for(int x = 0; x < image.data[0].size(); x++) {
                final int color = image.data[y].data[x];
                final int r = (color & 0xffffff) >> 16;
                final int g = (color & 0xffff) >> 8;
                final int b = (color & 0xff);
                checksum += r + g + b;
            }
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
        JGFInstrumentor.addOpsToTimer("Section3:RayTracer:Run", image.size()
                * image.data[0].size());
        JGFInstrumentor.addOpsToTimer("Section3:RayTracer:Total", 1);
        
        JGFInstrumentor.printTimer("Section3:RayTracer:Init");
        JGFInstrumentor.printTimer("Section3:RayTracer:Run");
        JGFInstrumentor.printTimer("Section3:RayTracer:Total");
    }
    
    private static class RayTracerTask implements Runnable {
        
        @_Mutable private final Slice<IntArray> imagePart;
        private final Scene scene;
        
        public RayTracerTask(@Mutable final Slice<IntArray> image,
                final Scene scene) {
            this.imagePart = image;
            this.scene = scene;
        }
        
        public void run() {
            imagePart.registerNewOwner();
            
            final RayTracer tracer = new RayTracer(scene);
            tracer.render(imagePart);
            
            imagePart.releasePassed();
        }
    }
}
