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
 * Base class for all the security objects, namely in terms of providing a
 * consistent means of identifying each such object. Also provides some methods
 * for writing out debug messages.
 *
 * @author H W Yau
 * @version $Revision: 1.13 $ $Date: 1999/02/16 18:51:58 $
 */
public class Path {
    
    /**
     * Simple string name.
     */
    private final String name;
    
    /**
     * The start date for the path, in YYYYMMDD format.
     */
    private final int startDate;
    /**
     * The end date for the path, in YYYYMMDD format.
     */
    private final int endDate;
    /**
     * The change in time between two successive data values.
     */
    private final double dTime;
    
    public Path(final String name, final int startDate, final int endDate,
            final double dTime) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dTime = dTime;
    }
    
    // ------------------------------------------------------------------------
    // Methods.
    // ------------------------------------------------------------------------
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
}
