package rolez.util;

/**
 * This class contains two methods for measuring time differences: {@link #go()} and {@link #get()}.
 * Simply use {@link #get()} to see how much time passed since the last call to {@link #go()}.
 * 
 * @author Michael
 */
public class StopWatch {
    
    private volatile long goTime = 0;
    
    /**
     * Saves the current system time.
     */
    public StopWatch go() {
        goTime = System.nanoTime();
        return this;
    }
    
    /**
     * @return the elapsed time (in seconds) since the last call to {@link #go()}. The result is
     *         returned as a double for now, since there is no <code>long</code> type in Rolez yet.
     * @throws IllegalStateException
     *             If {@link #go()} has not been called yet.
     */
    public double get() {
        if(goTime == 0)
            throw new IllegalStateException("go() has not been called yet");
        return (System.nanoTime() - goTime) / 1000000000.0;
    }
}
