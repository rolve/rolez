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
import java.util.Enumeration;
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
public class RatePath extends Path {
    
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
    
    /**
     * An instance variable, for storing the rate's path values itself.
     */
    private final double[] pathValue;
    
    // ------------------------------------------------------------------------
    // Constructors.
    // ------------------------------------------------------------------------
    
    public RatePath(final String name, final int startDate, final int endDate,
            final double dTime, final double[] pathValue) {
        super(name, startDate, endDate, dTime);
        
        this.pathValue = pathValue;
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
        super(mc.get_name(), mc.get_startDate(), mc.get_endDate(), mc
                .get_dTime());
        
        // Fields pertaining to RatePath object itself.
        pathValue = mc.get_pathValue();
    }
    
    /**
     * Method for calculating the returns on a given rate path, via the
     * definition for the instantaneous compounded return. u_i =
     * \ln{\frac{S_i}{S_{i-1}}}
     * 
     * @return the return, as defined.
     */
    public ReturnPath getReturnCompounded() {
        final double[] returnPathValue = new double[pathValue.length];
        returnPathValue[0] = 0.0;
        for(int i = 1; i < pathValue.length; i++) {
            returnPathValue[i] = Math.log(pathValue[i] / pathValue[i - 1]);
        }
        final ReturnPath rPath = new ReturnPath(get_name(), get_startDate(),
                get_endDate(), get_dTime(), returnPathValue);
        rPath.estimatePath();
        return(rPath);
    }
    
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
     * @return A rate path with the read data
     */
    public static RatePath readRatesFile(final String dirName,
            final String filename) {
        final File ratesFile = new File(dirName, filename);
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(ratesFile));
        } catch(final FileNotFoundException e) {
            throw new AssertionError(e);
        }
        
        // Proceed to read all the lines of data into a Vector object.
        int iLine = 0;
        
        String aLine;
        final Vector<String> allLines = new Vector<String>(100);
        try {
            while((aLine = in.readLine()) != null) {
                iLine++;
                allLines.addElement(aLine);
            }
            in.close();
        } catch(final IOException e) {
            throw new RuntimeException("Problem reading data from the file", e);
        }
        
        // Now create an array to store the rates data.
        final double[] pathValue = new double[iLine];
        final int[] pathDate = new int[iLine];
        
        iLine = 0;
        for(final Enumeration<String> e = allLines.elements(); e
                .hasMoreElements();) {
            aLine = e.nextElement();
            final String[] field = aLine.split(",");
            final int aDate = Integer.parseInt("19" + field[0]);
            
            final double aPathValue = Double.parseDouble(field[DATUMFIELD]);
            if((aDate <= MINIMUMDATE) || (Math.abs(aPathValue) < EPSILON))
                throw new AssertionError("erroneous data in " + filename
                        + " indexed by date=" + field[0] + ".");
            else {
                pathDate[iLine] = aDate;
                pathValue[iLine] = aPathValue;
                iLine++;
            }
        }
        
        return new RatePath(filename, pathDate[0], pathDate[iLine - 1],
                1.0 / 365.0, pathValue);
    }
}
