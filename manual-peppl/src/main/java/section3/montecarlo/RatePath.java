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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

/**
 * Class for recording the values in the time-dependent path of a security.
 * <p>
 * To Do list:
 * <ol>
 * <li><i>None!</i>
 * </ol>
 *
 * @author H W Yau
 * @version $Revision: 1.28 $ $Date: 1999/02/16 18:52:29 $
 */
public class RatePath extends PathId {
    
    /**
     * Class variable for determining which field in the stock data should be
     * used. This is currently set to point to the 'closing price'.
     */
    public static final int DATUMFIELD = 4;
    /**
     * Class variable to represent the minimal date, whence the stock prices
     * appear. Used to trap any potential problems with the data.
     */
    public static final int MINIMUMDATE = 19000101;
    /**
     * Class variable for defining what is meant by a small number, small enough
     * to cause an arithmetic overflow when dividing. According to the Java
     * Nutshell book, the actual range is +/-4.9406564841246544E-324
     */
    public static final double EPSILON = 10.0 * Double.MIN_VALUE;
    
    // ------------------------------------------------------------------------
    // Instance variables.
    // ------------------------------------------------------------------------
    /**
     * An instance variable, for storing the rate's path values itself.
     */
    private double[] pathValue;
    /**
     * An instance variable, for storing the corresponding date of the datum, in
     * 'YYYYMMDD' format.
     */
    private int[] pathDate;
    /**
     * The number of accepted values in the rate path.
     */
    private int nAcceptedPathValue = 0;
    
    // ------------------------------------------------------------------------
    // Constructors.
    // ------------------------------------------------------------------------
    
    /**
     * Constructor, where the user specifies the directory and filename in from
     * which the data should be read.
     *
     * @param dirName
     *            directory
     * @param filename
     *            file
     */
    public RatePath(final String dirName, final String filename) {
        set_prompt("RatePath> ");
        set_DEBUG(true);
        readRatesFile(dirName, filename);
    }
    
    /**
     * Constructor, for use by the Monte Carlo generator, when it wishes to
     * represent its findings as a RatePath object.
     *
     * @param mc
     *            the Monte Carlo generator object, whose data are to be copied
     *            over.
     */
    public RatePath(final MonteCarloPath mc) {
        // Fields pertaining to the parent PathId object:
        set_name(mc.get_name());
        set_startDate(mc.get_startDate());
        set_endDate(mc.get_endDate());
        set_dTime(mc.get_dTime());
        
        // Fields pertaining to RatePath object itself.
        pathValue = mc.get_pathValue();
        nAcceptedPathValue = mc.get_nTimeSteps();
        
        // Note that currently the pathDate is neither declared, defined,
        // nor used in the MonteCarloPath object.
        pathDate = new int[nAcceptedPathValue];
    }
    
    /**
     * Constructor, for when there is no actual pathValue with which to
     * initialise.
     *
     * @param pathValueLength
     *            the length of the array containing the values for the path.
     * @param name
     *            the name to attach to the path.
     * @param startDate
     *            date from which the path is supposed to start, in 'YYYYMMDD'
     *            format.
     * @param endDate
     *            date from which the path is supposed to end, in 'YYYYMMDD'
     *            format.
     * @param dTime
     *            the time interval between successive path values, in fractions
     *            of a year.
     */
    public RatePath(final int pathValueLength, final String name,
            final int startDate, final int endDate, final double dTime) {
        set_name(name);
        set_startDate(startDate);
        set_endDate(endDate);
        set_dTime(dTime);
        set_prompt("RatePath> ");
        set_DEBUG(true);
        this.pathValue = new double[pathValueLength];
        this.nAcceptedPathValue = pathValue.length;
    }
    
    // ------------------------------------------------------------------------
    // Methods.
    // ------------------------------------------------------------------------
    /**
     * Routine to update this rate path with the values from another rate path,
     * via its pathValue array.
     *
     * @param operandPath
     *            the path value array to use for the update.
     */
    public void inc_pathValue(final double[] operandPath) {
        if(pathValue.length != operandPath.length)
            throw new DemoException(
                    "The path to update has a different size to the path to update with!");
        for(int i = 0; i < pathValue.length; i++)
            pathValue[i] += operandPath[i];
    }
    
    /**
     * Routine to scale this rate path by a constant.
     *
     * @param scale
     *            the constant with which to multiply to all the path values.
     */
    public void inc_pathValue(final double scale) {
        if(pathValue == null)
            throw new DemoException("Variable pathValue is undefined!");
        for(int i = 0; i < pathValue.length; i++)
            pathValue[i] *= scale;
    }
    
    // ------------------------------------------------------------------------
    // Accessor methods for class RatePath.
    // Generated by 'makeJavaAccessor.pl' script. HWY. 20th January 1999.
    // ------------------------------------------------------------------------
    /**
     * Accessor method for private instance variable <code>pathValue</code>.
     *
     * @return Value of instance variable <code>pathValue</code>.
     */
    public double[] get_pathValue() {
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
     * Accessor method for private instance variable <code>pathDate</code>.
     *
     * @return Value of instance variable <code>pathDate</code>.
     */
    public int[] get_pathDate() {
        if(this.pathDate == null)
            throw new DemoException("Variable pathDate is undefined!");
        return(this.pathDate);
    }
    
    /**
     * Set method for private instance variable <code>pathDate</code>.
     *
     * @param pathDate
     *            the value to set for the instance variable
     *            <code>pathDate</code>.
     */
    public void set_pathDate(final int[] pathDate) {
        this.pathDate = pathDate;
    }
    
    // ------------------------------------------------------------------------
    /**
     * Method to return the terminal value for a given rate path, as used in
     * derivative calculations.
     * 
     * @return The last value in the rate path.
     */
    public double getEndPathValue() {
        return(getPathValue(pathValue.length - 1));
    }
    
    /**
     * Method to return the value for a given rate path, at a given index.
     * <i>One may want to index this in a more user friendly manner!</i>
     * 
     * @param index
     *            the index on which to return the path value.
     * @return The value of the path at the designated index.
     */
    public double getPathValue(final int index) {
        return(pathValue[index]);
    }
    
    /**
     * Method for calculating the returns on a given rate path, via the
     * definition for the instantaneous compounded return. u_i =
     * \ln{\frac{S_i}{S_{i-1}}}
     * 
     * @return the return, as defined.
     */
    public ReturnPath getReturnCompounded() {
        if(pathValue == null || nAcceptedPathValue == 0) {
            throw new DemoException("The Rate Path has not been defined!");
        }
        final double[] returnPathValue = new double[nAcceptedPathValue];
        returnPathValue[0] = 0.0;
        try {
            for(int i = 1; i < nAcceptedPathValue; i++) {
                returnPathValue[i] = Math.log(pathValue[i] / pathValue[i - 1]);
            }
        } catch(final ArithmeticException aex) {
            throw new DemoException("Error in getReturnLogarithm:"
                    + aex.toString());
        }
        final ReturnPath rPath = new ReturnPath(returnPathValue,
                nAcceptedPathValue);
        //
        // Copy the PathId information to the ReturnPath object.
        rPath.copyInstanceVariables(this);
        rPath.estimatePath();
        return(rPath);
    }
    
    // ------------------------------------------------------------------------
    // Private methods.
    // ------------------------------------------------------------------------
    /**
     * Method for reading in data file, in a given format. Namely:
     * 
     * <pre>
     *       881003,0.0000,14.1944,13.9444,14.0832,2200050,0
     *       881004,0.0000,14.1668,14.0556,14.1668,1490850,0
     *       ...
     *       990108,35.8125,36.7500,35.5625,35.8125,4381200,0
     *       990111,35.8125,35.8750,34.8750,35.1250,3920800,0
     *       990112,34.8750,34.8750,34.0000,34.0625,3577500,0
     * </pre>
     * <p>
     * Where the fields represent, one believes, the following:
     * <ol>
     * <li>The date in 'YYMMDD' format</li>
     * <li>Open</li>
     * <li>High</li>
     * <li>Low</li>
     * <li>Last</li>
     * <li>Volume</li>
     * <li>Open Interest</li>
     * </ol>
     * One will probably make use of the closing price, but this can be
     * redefined via the class variable <code>DATUMFIELD</code>. Note that since
     * the read in data are then used to compute the return, this would be a
     * good place to trap for zero values in the data, which will cause all
     * sorts of problems.
     *
     * @param dirName
     *            the directory in which to search for the data file.
     * @param filename
     *            the data filename itself.
     */
    private void readRatesFile(final String dirName, final String filename) {
        final File ratesFile = new File(dirName, filename);
        BufferedReader in;
        if(!ratesFile.canRead()) {
            throw new DemoException("Cannot read the file "
                    + ratesFile.toString());
        }
        try {
            in = new BufferedReader(new FileReader(ratesFile));
        } catch(final FileNotFoundException fnfex) {
            throw new DemoException(fnfex.toString());
        }
        
        // Proceed to read all the lines of data into a Vector object.
        int iLine = 0;
        final int initNlines = 100;
        int nLines = 0;
        
        String aLine;
        final Vector<String> allLines = new Vector<String>(initNlines);
        try {
            while((aLine = in.readLine()) != null) {
                iLine++;
                
                // Note, I'm not entirely sure whether the object passed in is
                // copied by value, or just its reference.
                allLines.addElement(aLine);
            }
            in.close();
        } catch(final IOException ioex) {
            throw new DemoException("Problem reading data from the file "
                    + ioex.toString());
        }
        nLines = iLine;
        //
        // Now create an array to store the rates data.
        this.pathValue = new double[nLines];
        this.pathDate = new int[nLines];
        nAcceptedPathValue = 0;
        iLine = 0;
        for(final java.util.Enumeration<String> e = allLines.elements(); e
                .hasMoreElements();) {
            aLine = e.nextElement();
            final String[] field = aLine.split(",");
            final int aDate = Integer.parseInt("19" + field[0]);
            //
            // static double Double.parseDouble() method is a feature of JDK1.2!
            final double aPathValue = Double.valueOf(field[DATUMFIELD])
                    .doubleValue();
            if((aDate <= MINIMUMDATE) || (Math.abs(aPathValue) < EPSILON)) {
                dbgPrintln("Skipped erroneous data in " + filename
                        + " indexed by date=" + field[0] + ".");
            }
            else {
                pathDate[iLine] = aDate;
                pathValue[iLine] = aPathValue;
                iLine++;
            }
        }
        //
        // Record the actual number of accepted data points.
        nAcceptedPathValue = iLine;
        //
        // Now to fill in the structures from the 'PathId' class.
        set_name(filename);
        set_startDate(pathDate[0]);
        set_endDate(pathDate[nAcceptedPathValue - 1]);
        set_dTime(1.0 / 365.0);
    }
}
