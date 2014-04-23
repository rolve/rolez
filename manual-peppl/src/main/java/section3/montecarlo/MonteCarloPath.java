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

package section3.montecarlo;

import java.util.Random;

/**
 * Class representing the paths generated by the Monte Carlo engine.
 * <p>
 * To do list:
 * <ol>
 * <li><code>double[] pathDate</code> is not simulated.</li>
 * </ol>
 *
 * @author H W Yau
 * @version $Revision: 1.18 $ $Date: 1999/02/16 18:51:28 $
 */
public class MonteCarloPath extends PathId {
    
    /**
     * Class variable for determining which field in the stock data should be
     * used. This is currently set to point to the 'closing price', as defined
     * in class RatePath.
     */
    public static final int DATUMFIELD = RatePath.DATUMFIELD;
    
    // ------------------------------------------------------------------------
    // Instance variables.
    // ------------------------------------------------------------------------
    /**
     * Random fluctuations generated as a series of random numbers with given
     * distribution.
     */
    private double[] fluctuations;
    /**
     * The path values from which the random fluctuations are used to update.
     */
    private double[] pathValue;
    /**
     * Integer flag for determining how the return was calculated, when used to
     * calculate the mean drift and volatility parameters.
     */
    private int returnDefinition = 0;
    /**
     * Value for the mean drift, for use in the generation of the random path.
     */
    private double expectedReturnRate = Double.NaN;
    /**
     * Value for the volatility, for use in the generation of the random path.
     */
    private double volatility = Double.NaN;
    /**
     * Number of time steps for which the simulation should act over.
     */
    private int nTimeSteps = 0;
    
    // ------------------------------------------------------------------------
    // Constructors.
    // ------------------------------------------------------------------------
    /**
     * Default constructor. Needed by the HPT library to start create new
     * instances of this class. The instance variables for this should then be
     * initialised with the <code>setInitAllTasks()</code> method.
     */
    public MonteCarloPath() {
        set_prompt("MonteCarloPath> ");
        set_DEBUG(true);
    }
    
    // ------------------------------------------------------------------------
    // Methods.
    // ------------------------------------------------------------------------
    /**
     * Set method for private instance variable <code>fluctuations</code>.
     *
     * @param fluctuations
     *            the value to set for the instance variable
     *            <code>fluctuations</code>.
     */
    public void set_fluctuations(final double[] fluctuations) {
        this.fluctuations = fluctuations;
    }
    
    /**
     * Accessor method for private instance variable <code>pathValue</code>.
     *
     * @return Value of instance variable <code>pathValue</code>.
     * @exception DemoException
     *                thrown if instance variable <code>pathValue</code> is
     *                undefined.
     */
    public double[] get_pathValue() throws DemoException {
        if(this.pathValue == null)
            throw new DemoException("Variable pathValue is undefined!");
        return(this.pathValue);
    }
    
    /**
     * Set method for private instance variable <code>pathValue</code>.
     *
     * @param pathValue
     *            the value to set for the instance variable
     *            <code>pathValue</code>.
     */
    public void set_pathValue(final double[] pathValue) {
        this.pathValue = pathValue;
    }
    
    /**
     * Set method for private instance variable <code>returnDefinition</code>.
     *
     * @param returnDefinition
     *            the value to set for the instance variable
     *            <code>returnDefinition</code>.
     */
    public void set_returnDefinition(final int returnDefinition) {
        this.returnDefinition = returnDefinition;
    }
    
    /**
     * Set method for private instance variable <code>expectedReturnRate</code>.
     *
     * @param expectedReturnRate
     *            the value to set for the instance variable
     *            <code>expectedReturnRate</code>.
     */
    public void set_expectedReturnRate(final double expectedReturnRate) {
        this.expectedReturnRate = expectedReturnRate;
    }
    
    /**
     * Set method for private instance variable <code>volatility</code>.
     *
     * @param volatility
     *            the value to set for the instance variable
     *            <code>volatility</code>.
     */
    public void set_volatility(final double volatility) {
        this.volatility = volatility;
    }
    
    /**
     * Accessor method for private instance variable <code>nTimeSteps</code>.
     *
     * @return Value of instance variable <code>nTimeSteps</code>.
     * @exception DemoException
     *                thrown if instance variable <code>nTimeSteps</code> is
     *                undefined.
     */
    public int get_nTimeSteps() throws DemoException {
        if(this.nTimeSteps == 0)
            throw new DemoException("Variable nTimeSteps is undefined!");
        return(this.nTimeSteps);
    }
    
    /**
     * Set method for private instance variable <code>nTimeSteps</code>.
     *
     * @param nTimeSteps
     *            the value to set for the instance variable
     *            <code>nTimeSteps</code>.
     */
    public void set_nTimeSteps(final int nTimeSteps) {
        this.nTimeSteps = nTimeSteps;
    }
    
    /**
     * Method for calculating the sequence of fluctuations, based around a
     * Gaussian distribution of given mean and variance, as defined in this
     * class' instance variables. Mapping from Gaussian distribution of (0,1) to
     * (mean-drift,volatility) is done via Ito's lemma on the log of the stock
     * price.
     * 
     * @param randomSeed
     *            The psuedo-random number seed value, to start off a given
     *            sequence of Gaussian fluctuations.
     * @exception DemoException
     *                thrown if there are any problems with the computation.
     */
    public void computeFluctuationsGaussian(final long randomSeed)
            throws DemoException {
        if(nTimeSteps > fluctuations.length)
            throw new DemoException(
                    "Number of timesteps requested is greater than the allocated array!");
        //
        // First, make use of the passed in seed value.
        Random rnd;
        if(randomSeed == -1) {
            rnd = new Random();
        }
        else {
            rnd = new Random(randomSeed);
        }
        //
        // Determine the mean and standard-deviation, from the mean-drift and
        // volatility.
        final double mean = (expectedReturnRate - 0.5 * volatility * volatility)
                * get_dTime();
        final double sd = volatility * Math.sqrt(get_dTime());
        double gauss;
        for(int i = 0; i < nTimeSteps; i++) {
            gauss = rnd.nextGaussian();
            //
            // Now map this onto a general Gaussian of given mean and variance.
            fluctuations[i] = mean + sd * gauss;
        }
    }
    
    /**
     * Method for calculating the sequence of fluctuations, based around a
     * Gaussian distribution of given mean and variance, as defined in this
     * class' instance variables. Mapping from Gaussian distribution of (0,1) to
     * (mean-drift,volatility) is done via Ito's lemma on the log of the stock
     * price. This overloaded method is for when the random seed should be
     * decided by the system.
     * 
     * @exception DemoException
     *                thrown if there are any problems with the computation.
     */
    public void computeFluctuationsGaussian() throws DemoException {
        computeFluctuationsGaussian(-1);
    }
    
    /**
     * Method for calculating the corresponding rate path, given the
     * fluctuations and starting rate value.
     * 
     * @param startValue
     *            the starting value of the rate path, to be updated with the
     *            precomputed fluctuations.
     * @exception DemoException
     *                thrown if there are any problems with the computation.
     */
    public void computePathValue(final double startValue) throws DemoException {
        pathValue[0] = startValue;
        if(returnDefinition == ReturnPath.COMPOUNDED
                || returnDefinition == ReturnPath.NONCOMPOUNDED) {
            for(int i = 1; i < nTimeSteps; i++) {
                pathValue[i] = pathValue[i - 1] * Math.exp(fluctuations[i]);
            }
        }
        else {
            throw new DemoException("Unknown or undefined update method.");
        }
    }
}
