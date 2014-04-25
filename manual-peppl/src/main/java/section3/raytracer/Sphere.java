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

public class Sphere extends Primitive {
    private final Vec c;
    private final double r, r2;
    
    private final Vec v = new Vec(); // temporary vec used to minimize the
                                     // memory load
    
    public Sphere(final Vec center, final double radius) {
        c = center;
        r = radius;
        r2 = r * r;
    }
    
    @Override
    public Intersection intersect(final Ray ry) {
        double b, disc, t;
        v.sub2(c, ry.P);
        b = Vec.dot(v, ry.D);
        disc = b * b - Vec.dot(v, v) + r2;
        if(disc < 0.0) {
            return null;
        }
        disc = Math.sqrt(disc);
        t = (b - disc < 1e-6) ? b + disc : b - disc;
        if(t < 1e-6) {
            return null;
        }
        
        return new Intersection(t, Vec.dot(v, v) > r2 + 1e-6 ? 1 : 0, this, mat);
    }
    
    @Override
    public Vec normal(final Vec p) {
        Vec result;
        result = Vec.sub(p, c);
        result.normalize();
        return result;
    }
    
    @Override
    public String toString() {
        return "Sphere {" + c.toString() + "," + r + "}";
    }
    
    @Override
    public Vec getCenter() {
        return c;
    }
}
