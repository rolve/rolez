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
public class Vec {
    
    /**
     * The x coordinate
     */
    public double x;
    
    /**
     * The y coordinate
     */
    public double y;
    
    /**
     * The z coordinate
     */
    public double z;
    
    /**
     * Constructor
     * 
     * @param a
     *            the x coordinate
     * @param b
     *            the y coordinate
     * @param c
     *            the z coordinate
     */
    public Vec(final double a, final double b, final double c) {
        x = a;
        y = b;
        z = c;
    }
    
    public Vec(final Vec a) {
        x = a.x;
        y = a.y;
        z = a.z;
    }
    
    /**
     * Default (0,0,0) constructor
     */
    public Vec() {
        x = 0.0;
        y = 0.0;
        z = 0.0;
    }
    
    /**
     * Add a vector to the current vector
     * 
     * @param a
     *            The vector to be added
     */
    public final void add(final Vec a) {
        x += a.x;
        y += a.y;
        z += a.z;
    }
    
    /**
     * Adds vector such as: this+=sB
     * 
     * @param s
     *            The multiplier
     * @param b
     *            The vector to be added
     */
    public final void adds(final double s, final Vec b) {
        x += s * b.x;
        y += s * b.y;
        z += s * b.z;
    }
    
    /**
     * Adds vector such as: this+=sB
     * 
     * @param s
     *            The multiplier
     * @param b
     *            The vector to be added
     */
    public final void adds(final double s, final ImmutableVec b) {
        x += s * b.x;
        y += s * b.y;
        z += s * b.z;
    }
    
    /**
     * Substracs two vectors
     * 
     * @param a
     *            first vector
     * @param b
     *            second vector
     * @return resulting vector
     */
    public static Vec sub(final Vec a, final Vec b) {
        return new Vec(a.x - b.x, a.y - b.y, a.z - b.z);
    }
    
    /**
     * Substracs two vectors
     * 
     * @param a
     *            first vector
     * @param b
     *            second vector
     * @return resulting vector
     */
    public static Vec sub(final Vec a, final ImmutableVec b) {
        return new Vec(a.x - b.x, a.y - b.y, a.z - b.z);
    }
    
    /**
     * Substracts two vects and places the results in the current vector Used
     * for speedup with local variables -there were too much Vec to be gc'ed
     * Consumes about 10 units, whether sub consumes nearly 999 units!! cf
     * thinking in java p. 831,832
     * 
     * @param a
     *            first vector
     * @param b
     *            second vector
     */
    public final void sub2(final ImmutableVec a, final Vec b) {
        this.x = a.x - b.x;
        this.y = a.y - b.y;
        this.z = a.z - b.z;
    }
    
    public final ImmutableVec immutable() {
        return new ImmutableVec(x, y, z);
    }
    
    public static Vec cross(final Vec a, final Vec b) {
        return new Vec(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y
                - a.y * b.x);
    }
    
    public static double dot(final Vec a, final Vec b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }
    
    public static Vec comb(final double a, final Vec A, final double b,
            final Vec B) {
        return new Vec(a * A.x + b * B.x, a * A.y + b * B.y, a * A.z + b * B.z);
    }
    
    public final void scale(final double t) {
        x *= t;
        y *= t;
        z *= t;
    }
    
    public final void negate() {
        x = -x;
        y = -y;
        z = -z;
    }
    
    public final void normalize() {
        final double len = Math.sqrt(x * x + y * y + z * z);
        if(len > 0.0) {
            x /= len;
            y /= len;
            z /= len;
        }
    }
    
    @Override
    public final String toString() {
        return "<" + x + "," + y + "," + z + ">";
    }
}
