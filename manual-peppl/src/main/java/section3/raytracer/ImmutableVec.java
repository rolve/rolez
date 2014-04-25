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
 *                 Original version of this code by                        *
 *            Florian Doyon (Florian.Doyon@sophia.inria.fr)                *
 *              and  Wilfried Klauser (wklauser@acm.org)                   *
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 2001.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/

package section3.raytracer;

/**
 * This class reflects the 3d vectors used in 3d computations
 */
public class ImmutableVec {
    
    public static final ImmutableVec O = new ImmutableVec(0, 0, 0);
    
    /**
     * The x coordinate
     */
    public final double x;
    
    /**
     * The y coordinate
     */
    public final double y;
    
    /**
     * The z coordinate
     */
    public final double z;
    
    /**
     * Constructor
     * 
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @param z
     *            the z coordinate
     */
    public ImmutableVec(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public ImmutableVec(final ImmutableVec a) {
        x = a.x;
        y = a.y;
        z = a.z;
    }
    
    @Override
    public final String toString() {
        return "<" + x + "," + y + "," + z + ">";
    }
}
