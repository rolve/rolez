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

package ch.trick17.peppl.manual.raytracer;

public class Sphere extends Primitive {
    private final ImmutableVec c;
    private final double r, r2;
    
    public Sphere(final Material mat, final ImmutableVec center, final double radius) {
        super(mat);
        c = center;
        r = radius;
        r2 = r * r;
    }
    
    @Override
    public Intersection intersect(final Ray ry, final Vec temp) {
        double b, disc, t;
        temp.sub2(c, ry.origin);
        b = Vec.dot(temp, ry.dir);
        disc = b * b - Vec.dot(temp, temp) + r2;
        if(disc < 0.0) {
            return null;
        }
        disc = Math.sqrt(disc);
        t = (b - disc < 1e-6) ? b + disc : b - disc;
        if(t < 1e-6) {
            return null;
        }
        
        return new Intersection(t,
                Vec.dot(temp, temp) > r2 + 1e-6 ? 1 : 0, this, mat);
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
    public ImmutableVec getCenter() {
        return c;
    }
}
