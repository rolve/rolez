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

/**
 * Class for representing the returns of a given security.
 * <p>
 * To do list:
 * <ol>
 * <li>Define a window over which the mean drift and volatility are calculated.</li>
 * <li>Hash table to reference {DATE}->{pathValue-index}.</li>
 * </ol>
 *
 * @author H W Yau
 * @version $Revision: 1.21 $ $Date: 1999/02/16 18:52:41 $
 */
public class ReturnPath extends PathId {
    
    /**
     * Flag for indicating one of the return definitions, via: u_i =
     * \ln{\frac{S_i}{S_{i-1}}} corresponding to the instantaneous compounded
     * return.
     */
    public static final int COMPOUNDED = 1;
    
    /**
     * Flag for indicating one of the return definitions, via: u_i = \frac{S_i -
     * S_{i-1}}{S_i} corresponding to the instantaneous non-compounded return.
     */
    public static final int NONCOMPOUNDED = 2;
    
    // ------------------------------------------------------------------------
    // Instance variables.
    // ------------------------------------------------------------------------
    /**
     * An instance variable, for storing the return values.
     */
    private final double[] pathValue;
    /**
     * The number of accepted values in the rate path.
     */
    private final int nPathValue;
    /**
     * Integer flag for indicating how the return was calculated.
     */
    private final int returnDefinition;
    /**
     * Value for the expected return rate.
     */
    private double expectedReturnRate = Double.NaN;
    /**
     * Value for the volatility, calculated from the return data.
     */
    private double volatility = Double.NaN;
    /**
     * Value for the volatility-squared, a more natural quantity to use for many
     * of the calculations.
     */
    private double volatility2 = Double.NaN;
    /**
     * Value for the mean of this return.
     */
    private double mean = Double.NaN;
    /**
     * Value for the variance of this return.
     */
    private double variance = Double.NaN;
    
    // ------------------------------------------------------------------------
    // Constructors.
    // ------------------------------------------------------------------------
    
    /**
     * Another constructor.
     *
     * @param pathValue
     *            for creating a return path with a precomputed path value.
     *            Indexed from 1 to <code>nPathArray-1</code>.
     * @param nPathValue
     *            the number of accepted data points in the array.
     * @param returnDefinition
     *            to tell this class how the return path values were computed.
     */
    public ReturnPath(final double[] pathValue, final int nPathValue,
            final int returnDefinition) {
        set_prompt("ReturnPath> ");
        set_DEBUG(true);
        this.pathValue = pathValue;
        this.nPathValue = nPathValue;
        this.returnDefinition = returnDefinition;
    }
    
    // ------------------------------------------------------------------------
    // Methods.
    // ------------------------------------------------------------------------
    /**
     * Accessor method for private instance variable
     * <code>returnDefinition</code>.
     *
     * @return Value of instance variable <code>returnDefinition</code>.
     * @exception DemoException
     *                thrown if instance variable <code>returnDefinition</code>
     *                is undefined.
     */
    public int get_returnDefinition() throws DemoException {
        if(this.returnDefinition == 0)
            throw new DemoException("Variable returnDefinition is undefined!");
        return(this.returnDefinition);
    }
    
    /**
     * Accessor method for private instance variable
     * <code>expectedReturnRate</code>.
     *
     * @return Value of instance variable <code>expectedReturnRate</code>.
     * @exception DemoException
     *                thrown if instance variable
     *                <code>expectedReturnRate</code> is undefined.
     */
    public double get_expectedReturnRate() throws DemoException {
        if(this.expectedReturnRate == Double.NaN)
            throw new DemoException("Variable expectedReturnRate is undefined!");
        return(this.expectedReturnRate);
    }
    
    /**
     * Accessor method for private instance variable <code>volatility</code>.
     *
     * @return Value of instance variable <code>volatility</code>.
     * @exception DemoException
     *                thrown if instance variable <code>volatility</code> is
     *                undefined.
     */
    public double get_volatility() throws DemoException {
        if(this.volatility == Double.NaN)
            throw new DemoException("Variable volatility is undefined!");
        return(this.volatility);
    }
    
    // ------------------------------------------------------------------------
    /**
     * Method to calculate the expected return rate from the return data, using
     * the relationship: \mu = \frac{\bar{u}}{\Delta t} + \frac{\sigma^2}{2}
     *
     * @exception DemoException
     *                thrown one tries to obtain an undefined variable.
     */
    public void computeExpectedReturnRate() throws DemoException {
        this.expectedReturnRate = mean / get_dTime() + 0.5 * volatility2;
    }
    
    /**
     * Method to calculate <code>volatility</code> and <code>volatility2</code>
     * from the return path data, using the relationship, based on the
     * precomputed <code>variance</code>. \sigma^2 = s^2\Delta t
     * 
     * @exception DemoException
     *                thrown if one of the quantites in the computation are
     *                undefined.
     */
    public void computeVolatility() throws DemoException {
        if(this.variance == Double.NaN)
            throw new DemoException("Variable variance is not defined!");
        this.volatility2 = variance / get_dTime();
        this.volatility = Math.sqrt(volatility2);
    }
    
    /**
     * Method to calculate the mean of the return, for use by other
     * calculations.
     *
     * @exception DemoException
     *                thrown if <code>nPathValue</code> is undefined.
     */
    public void computeMean() throws DemoException {
        if(this.nPathValue == 0)
            throw new DemoException("Variable nPathValue is undefined!");
        this.mean = 0.0;
        for(int i = 1; i < nPathValue; i++) {
            mean += pathValue[i];
        }
        this.mean /= (nPathValue - 1.0);
    }
    
    /**
     * Method to calculate the variance of the retrun, for use by other
     * calculations.
     *
     * @exception DemoException
     *                thrown if the <code>mean</code> or <code>nPathValue</code>
     *                values are undefined.
     */
    public void computeVariance() throws DemoException {
        if(this.mean == Double.NaN || this.nPathValue == 0)
            throw new DemoException(
                    "Variable mean and/or nPathValue are undefined!");
        this.variance = 0.0;
        for(int i = 1; i < nPathValue; i++) {
            variance += (pathValue[i] - mean) * (pathValue[i] - mean);
        }
        this.variance /= (nPathValue - 1.0);
    }
    
    /**
     * A single method for invoking all the necessary methods which estimate the
     * parameters.
     *
     * @exception DemoException
     *                thrown if there is a problem reading any variables.
     */
    public void estimatePath() throws DemoException {
        computeMean();
        computeVariance();
        computeExpectedReturnRate();
        computeVolatility();
    }
}
