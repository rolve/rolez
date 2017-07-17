package rolez.checked.util;

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
     * @return the elapsed time (in seconds) since the last call to {@link #go()}.
     * @throws IllegalStateException
     *             If {@link #go()} has not been called yet.
     */
    public double get() {
        return getNs() / 1000000000.0;
    }
    
    /**
     * @return the elapsed time (in milliseconds) since the last call to {@link #go()}.
     * @throws IllegalStateException
     *             If {@link #go()} has not been called yet.
     */
    public long getMs() {
        return getNs() / 1000000;
    }
    
    /**
     * @return the elapsed time (in nanoseconds) since the last call to {@link #go()}.
     * @throws IllegalStateException
     *             If {@link #go()} has not been called yet.
     */
    public long getNs() {
        if(goTime == 0)
            throw new IllegalStateException("go() has not been called yet");
        return(System.nanoTime() - goTime);
    }
}
