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

final public class Ray {
    public Vec origin, dir;
    
    public Ray(final Vec origin, final Vec dir) {
        this.origin = new Vec(origin.x, origin.y, origin.z);
        this.dir = new Vec(dir.x, dir.y, dir.z);
        this.dir.normalize();
    }
    
    public Ray() {
        origin = new Vec();
        dir = new Vec();
    }
    
    public Vec point(final double t) {
        return new Vec(origin.x + dir.x * t, origin.y + dir.y * t, origin.z + dir.z
                * t);
    }
    
    @Override
    public String toString() {
        return "{" + origin.toString() + " -> " + dir.toString() + "}";
    }
}
