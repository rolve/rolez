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
 *      This version copyright (c) The University of Edinburgh, 1999.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/

package jgfutil;

import java.util.Hashtable;

public class JGFInstrumentor {
    
    private static Hashtable timers;
    private static Hashtable data;
    
    static {
        timers = new Hashtable();
        data = new Hashtable();
    }
    
    public static synchronized void addTimer(final String name) {
        
        if(timers.containsKey(name)) {
            System.out.println("JGFInstrumentor.addTimer: warning -  timer "
                    + name + " already exists");
        }
        else {
            timers.put(name, new JGFTimer(name));
        }
    }
    
    public static synchronized void addTimer(final String name,
            final String opname) {
        
        if(timers.containsKey(name)) {
            System.out.println("JGFInstrumentor.addTimer: warning -  timer "
                    + name + " already exists");
        }
        else {
            timers.put(name, new JGFTimer(name, opname));
        }
        
    }
    
    public static synchronized void addTimer(final String name,
            final String opname, final int size) {
        
        if(timers.containsKey(name)) {
            System.out.println("JGFInstrumentor.addTimer: warning -  timer "
                    + name + " already exists");
        }
        else {
            timers.put(name, new JGFTimer(name, opname, size));
        }
        
    }
    
    public static synchronized void startTimer(final String name) {
        if(timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).start();
        }
        else {
            System.out.println("JGFInstrumentor.startTimer: failed -  timer "
                    + name + " does not exist");
        }
        
    }
    
    public static synchronized void stopTimer(final String name) {
        if(timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).stop();
        }
        else {
            System.out.println("JGFInstrumentor.stopTimer: failed -  timer "
                    + name + " does not exist");
        }
    }
    
    public static synchronized void addOpsToTimer(final String name,
            final double count) {
        if(timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).addops(count);
        }
        else {
            System.out
                    .println("JGFInstrumentor.addOpsToTimer: failed -  timer "
                            + name + " does not exist");
        }
    }
    
    public static synchronized void addTimeToTimer(final String name,
            final double added_time) {
        if(timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).addtime(added_time);
        }
        else {
            System.out
                    .println("JGFInstrumentor.addTimeToTimer: failed -  timer "
                            + name + " does not exist");
        }
        
    }
    
    public static synchronized double readTimer(final String name) {
        double time;
        if(timers.containsKey(name)) {
            time = ((JGFTimer) timers.get(name)).time;
        }
        else {
            System.out.println("JGFInstrumentor.readTimer: failed -  timer "
                    + name + " does not exist");
            time = 0.0;
        }
        return time;
    }
    
    public static synchronized void resetTimer(final String name) {
        if(timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).reset();
        }
        else {
            System.out.println("JGFInstrumentor.resetTimer: failed -  timer "
                    + name + " does not exist");
        }
    }
    
    public static synchronized void printTimer(final String name) {
        if(timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).print();
        }
        else {
            System.out.println("JGFInstrumentor.printTimer: failed -  timer "
                    + name + " does not exist");
        }
    }
    
    public static synchronized void printperfTimer(final String name) {
        if(timers.containsKey(name)) {
            ((JGFTimer) timers.get(name)).printperf();
        }
        else {
            System.out.println("JGFInstrumentor.printTimer: failed -  timer "
                    + name + " does not exist");
        }
    }
    
    public static synchronized void storeData(final String name,
            final Object obj) {
        data.put(name, obj);
    }
    
    public static synchronized void retrieveData(final String name, Object obj) {
        obj = data.get(name);
    }
    
    public static synchronized void printHeader(final int section,
            final int size, final int nthreads) {
        
        String header, base;
        
        header = "";
        base = "Java Grande Forum Thread Benchmark Suite - Version 1.0 - Section ";
        
        switch(section) {
        case 1:
            header = base + "1";
            break;
        case 2:
            switch(size) {
            case 0:
                header = base + "2 - Size A";
                break;
            case 1:
                header = base + "2 - Size B";
                break;
            case 2:
                header = base + "2 - Size C";
                break;
            }
            break;
        case 3:
            switch(size) {
            case 0:
                header = base + "3 - Size A";
                break;
            case 1:
                header = base + "3 - Size B";
                break;
            }
            break;
        }
        
        System.out.println(header);
        
        if(nthreads == 1) {
            System.out.println("Executing on " + nthreads + " thread");
        }
        else {
            System.out.println("Executing on " + nthreads + " threads");
        }
        
        System.out.println("");
        
    }
    
}
