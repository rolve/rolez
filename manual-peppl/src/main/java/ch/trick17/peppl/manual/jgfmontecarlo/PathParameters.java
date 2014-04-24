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
 *      Original version of this code by Hon Yau (hwyau@epcc.ed.ac.uk)     *
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 2001.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/

package ch.trick17.peppl.manual.jgfmontecarlo;

import section3.montecarlo.ReturnPath;
import ch.trick17.peppl.lib.guard.GuardedObject;

/**
 * Class for defining the initialisation data for all tasks.
 *
 * @author H W Yau
 * @version $Revision: 1.10 $ $Date: 1999/02/16 18:52:53 $
 */
public class PathParameters extends GuardedObject {
    private final String name;
    private final int startDate;
    private final int endDate;
    private final double dTime;
    private final double expectedReturnRate;
    private final double volatility;
    private final int timeSteps;
    
    /**
     * Another constructor, slightly easier to use by having slightly fewer
     * arguments. Makes use of the "ReturnPath" object to accomplish this.
     *
     * @param obj
     *            Object used to define the instance variables which should be
     *            carried over to this object.
     * @param timeSteps
     *            The number of time steps which the Monte Carlo generator
     *            should make.
     */
    public PathParameters(final ReturnPath obj, final int timeSteps) {
        this.name = obj.get_name();
        this.startDate = obj.get_startDate();
        this.endDate = obj.get_endDate();
        this.dTime = obj.get_dTime();
        this.expectedReturnRate = obj.get_expectedReturnRate();
        this.volatility = obj.get_volatility();
        
        this.timeSteps = timeSteps;
    }
    
    /**
     * Accessor method for private instance variable <code>name</code>.
     *
     * @return Value of instance variable <code>name</code>.
     */
    public String get_name() {
        return(this.name);
    }
    
    /**
     * Accessor method for private instance variable <code>startDate</code>.
     *
     * @return Value of instance variable <code>startDate</code>.
     */
    public int get_startDate() {
        return(this.startDate);
    }
    
    /**
     * Accessor method for private instance variable <code>endDate</code>.
     *
     * @return Value of instance variable <code>endDate</code>.
     */
    public int get_endDate() {
        return(this.endDate);
    }
    
    /**
     * Accessor method for private instance variable <code>dTime</code>.
     *
     * @return Value of instance variable <code>dTime</code>.
     */
    public double get_dTime() {
        return(this.dTime);
    }
    
    /**
     * Accessor method for private instance variable
     * <code>expectedReturnRate</code>.
     *
     * @return Value of instance variable <code>expectedReturnRate</code>.
     */
    public double get_expectedReturnRate() {
        return(this.expectedReturnRate);
    }
    
    /**
     * Accessor method for private instance variable <code>volatility</code>.
     *
     * @return Value of instance variable <code>volatility</code>.
     */
    public double get_volatility() {
        return(this.volatility);
    }
    
    /**
     * Accessor method for private instance variable <code>timeSteps</code>.
     *
     * @return Value of instance variable <code>timeSteps</code>.
     */
    public int get_timeSteps() {
        return(this.timeSteps);
    }
    // ------------------------------------------------------------------------
}
